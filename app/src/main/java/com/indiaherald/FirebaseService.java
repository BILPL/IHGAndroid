package com.indiaherald;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class FirebaseService extends FirebaseMessagingService
{
    private String TAG = "FirebaseService";

    public static final String NOTIFICATION_CHANNEL_ID = "IHGNotificationChannel";
    public static final String NOTIFICATION_CHANNEL_NAME = "IHG Notification Hubs Channel";
    public static final String NOTIFICATION_CHANNEL_DESCRIPTION = "IHG Notification Hubs Channel";

    public static  int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    static Context ctx;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String nhMessage;
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            nhMessage = remoteMessage.getNotification().getBody();
        }
        else {
            nhMessage = remoteMessage.getData().values().iterator().next();
        }
        nhMessage = nhMessage.replace("{:PUSH:}","::");
        nhMessage = nhMessage.split("::")[0];
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        sendNotification(nhMessage);
    }

    private void sendNotification(String msg) {
    try {
        if(Utility.IsNullOrEmpty(msg))
            return;
      //  Activity2Person notificationMsg =
        Type listOfMyClassObject = new TypeToken<Activity2Person>() {}.getType();

        if(null == ctx)
            ctx = getApplicationContext();

        Gson gson = new Gson();
        Activity2Person notificationMsg = gson.fromJson(msg, listOfMyClassObject);
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(!Utility.IsNullOrEmpty(notificationMsg.URL)){
            intent.putExtra(Constants.NOTIFICATIONMSG, notificationMsg.URL);
            intent.setData(Uri.parse(notificationMsg.URL));
        }
        String imageUrl = !Utility.IsNullOrEmpty(notificationMsg.ImageURL) ? notificationMsg.ImageURL : "https://www.indiaherald.com/images/HP-Images/IndiaHerald-SQUARE-LOGO-1.jpg";

        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap bitmap = null;
        bitmap = Utility.getBitmapFromURL(imageUrl);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                ctx,
                NOTIFICATION_CHANNEL_ID)
                .setContentText(notificationMsg.RelevantDataDescription)
                .setContentTitle(notificationMsg.RelevantDataTitle)
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.not_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setNumber(Constants.NOTIFICATION_COUNTER)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);

        notificationBuilder.setContentIntent(contentIntent);
        NOTIFICATION_ID++;
        mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

    }catch(Exception ex){
        Log.e(TAG, "sendNotification: " + ex.getMessage() );
    }
    }

    public static void createChannelAndHandleNotifications(Context context) {
        ctx = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            channel.setShowBadge(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
