package com.indiaherald;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.google.firebase.messaging.RemoteMessage;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener;

import java.util.Map;
import java.util.Random;

import static android.content.ContentValues.TAG;

//This class was used as a listener for notifications
public class CustomNotificationListener implements NotificationListener {

    public static final String NOTIFICATION_CHANNEL_ID = "IHGNotificationChannel";
    private NotificationManager mNotificationManager;
    public static int NOTIFICATION_ID = 1;
    String KEY_REPLY = "key_reply";

    @Override
    public void onPushNotificationReceived(Context context, RemoteMessage message) {

        /* The following notification properties are available. */
        try {
            RemoteMessage.Notification notification = message.getNotification();
            Map<String, String> data = message.getData();
            if (data != null) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    Log.d("Test", "key, " + entry.getKey() + " value " + entry.getValue());
                }
            }
            sendNotification(data, context);
        } catch (Exception ex) {
            Log.d("Listener", "onPushNotificationReceived: ");
        }

    }

    private void sendNotification(Map<String, String> data, Context ctx) {
        try {
            mNotificationManager = (NotificationManager)
                    ctx.getSystemService(Context.NOTIFICATION_SERVICE);


            NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.not_icon)
                    .setContentText(data.get("body"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle(data.get("title"));

            String replyLabel = "Enter your reply here";

            //Initialise RemoteInput
            RemoteInput remoteInput = new RemoteInput.Builder(KEY_REPLY)
                    .setLabel(replyLabel)
                    .build();


            int randomRequestCode = new Random().nextInt(54325);

            //PendingIntent that restarts the current activity instance.
            // Intent resultIntent = new Intent(ctx, MainActivity.class);
            Intent resultIntent = new Intent(ctx, MainActivity.class);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (!Utility.IsNullOrEmpty(data.get("link"))) {
                resultIntent.putExtra(Constants.NOTIFICATIONMSG, data.get("link"));
                resultIntent.setData(Uri.parse(data.get("link")));
            }
            //Set a unique request code for this pending intent
            PendingIntent resultPendingIntent = PendingIntent.getActivity(ctx, randomRequestCode, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            //Notification Action with RemoteInput instance added.
            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                    android.R.drawable.sym_action_chat, "REPLY", resultPendingIntent)
                    .addRemoteInput(remoteInput)
                    .setAllowGeneratedReplies(true)
                    .build();

            //Notification.Action instance added to Notification Builder.
            builder.addAction(replyAction);

            Intent intent = new Intent(ctx, MainActivity.class);
            intent.putExtra("notificationId", NOTIFICATION_ID);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // PendingIntent dismissIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


            //builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "DISMISS", dismissIntent);

            //Create Notification.
            NotificationManager notificationManager =
                    (NotificationManager)
                            ctx.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(NOTIFICATION_ID++,
                    builder.build());

        } catch (Exception ex) {
            Log.e(TAG, "sendNotification: " + ex.getMessage());
        }
    }

}


