package com.indiaherald;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.androidbrowserhelper.trusted.LauncherActivity;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;

import java.util.ArrayList;
import java.util.Arrays;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int REQUEST_READ_CONTACTS = 79;
    ArrayList<User> listOfContacts = new ArrayList<>();
    public DatabaseHelper mydb;
    ArrayList mobileArray;
    String urlscheme = "";
    String deviceId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);
        Utility.SetUserPreferences(Constants.DeviceId, Utility.GetDeviceUniqueId(this), this);
        String regId =Utility.GetUserPreference(Constants.registrationID, this);
        ListProvider.populateListItem();
        if (null == mydb)
            mydb = new DatabaseHelper(this);


       // boolean isNotifenabled = isNotificationChannelEnabled(this, "IHGNotificationChannel");
        //This condition will enable device registration
        String deviceReg = Utility.GetUserPreference(Constants.DeviceUpdated, this);
        if(Utility.IsNullOrEmpty(deviceReg) || deviceReg.toLowerCase() != "true" ){
            Utility.RegisterOrUpdateDeviceToken(this);
        }
        deviceId = Utility.GetDeviceUniqueId(this);
        //This condition will disable multiple time registration for Notfications
        if(Utility.IsNullOrEmpty(regId)){
            //registerWithNotificationHubs();
            NotificationHub.setListener(new CustomNotificationListener());
            //Here we are registering the notification with server
            NotificationHub.start(this.getApplication(),NotificationSettings.HubName, NotificationSettings.HubListenConnectionString);

            NotificationHub.setInstallationSavedListener(i -> {
                Toast.makeText(this, "SUCCESS", Toast.LENGTH_LONG).show();
                String regID = NotificationHub.getInstallationId();
                Utility.SetUserPreferences(Constants.registrationID, regID, this);
            });
            NotificationHub.setInstallationSaveFailureListener(e -> Toast.makeText(this,e.getMessage(), Toast.LENGTH_LONG).show());
            NotificationHub.addTag(deviceId);
        }

        // ATTENTION: This was to handle app links from out side like Notifications and deep links.
        Intent appLinkIntent = getIntent();
        Uri appLinkData = appLinkIntent.getData();
        String widgetURL = appLinkIntent.getStringExtra(Constants.WidgetItem);
        if(null == appLinkData && Utility.IsNullOrEmpty(widgetURL)){
            urlscheme = "https://amp.indiaherald.com/heartbeat";
        }else if(!Utility.IsNullOrEmpty(widgetURL)){
            urlscheme = widgetURL;
        }
        else{
            urlscheme = appLinkData.toString();
       }
        if(Utility.isNetworkAvailable(this)){
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_CONTACTS)
                            == PackageManager.PERMISSION_GRANTED) {
                        StartPWA(mainActivity);
                    } else {
                        Intent intent = new Intent(mainActivity, SplashActivity.class);
                        startActivity(intent);
                        mainActivity.finish();
                    }
                }
            });
        }
        Button mButton = findViewById(R.id.btnRetry);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(Utility.isNetworkAvailable(MainActivity.this)){
                   StartPWA(MainActivity.this);
                }else{
                    Toast.makeText(getApplicationContext(),"No Internet Available",Toast.LENGTH_LONG).show();
                }
            }
        });

        deviceId = Utility.GetDeviceUniqueId(this);

    }

    public void StartPWA(Activity context){
        try {
            Intent intent = new Intent(this, com.google.androidbrowserhelper.trusted.LauncherActivity.class);
            intent.setData(Uri.parse(urlscheme));
            intent.putExtra("url",urlscheme);
            startActivityForResult(intent, 55);
            //startActivityForResult(intent, 99);
            context.finish();
        }catch(Exception ex){
            Log.e(TAG, "run: " + ex.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 55){
            //this.finish();
        }
    }

    public void registerWithNotificationHubs()
    {
        if (checkPlayServices()) {
            // Start IntentService to register this application with FCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Utility.isNetworkAvailable(this)){
            this.finish();
            // return;
        }

    }

    private void showNotificationAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
//set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
//set title
                .setTitle("Info")
//set message
                .setMessage("Please do allow Notifications to server you better.")
//set positive button
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what would happen when positive button is clicked
                        finish();
                    }
                })
//set negative button
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what should happen when negative button is clicked
                        Toast.makeText(getApplicationContext(),"Nothing Happened",Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog box that enables  users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported by Google Play Services.");
                ToastNotify("This device is not supported by Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }

    //A sample toast message to call everywhere if needed
    public void ToastNotify(final String notificationMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
                //TextView helloText = (TextView) findViewById(R.id.text_hello);
                // helloText.setText(notificationMessage);
            }
        });
    }

    //This method will let you know isNotification Channele enabled or not
    public boolean isNotificationChannelEnabled(Context context, @Nullable String channelId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!TextUtils.isEmpty(channelId)) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = manager.getNotificationChannel(channelId);
                return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
            }
            return false;
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
    }

//This method will let you know the concept of reading contacts
    public ArrayList ReadAndContacts() {
        ArrayList<String> nameList = new ArrayList<>();
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ContentResolver cr = getContentResolver();
                        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                        if ((cur != null ? cur.getCount() : 0) > 0) {
                            while (cur != null && cur.moveToNext()) {
                                User contact = new User();
                                String id = cur.getString(
                                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                                String name = cur.getString(cur.getColumnIndex(
                                        ContactsContract.Contacts.DISPLAY_NAME));
                                contact.Name = name;
                                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                                    Cursor pCur = cr.query(
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                            new String[]{id}, null);
                                    while (pCur.moveToNext()) {
                                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                                        if (Utility.IsNullOrEmpty(phoneNo)) {
                                            continue;
                                        }

                                        if (phoneNo.contains("*") || phoneNo.contains("#")) {
                                            continue;
                                        }

                                        phoneNo = phoneNo.trim().replace(" ", Constants.EMPTY_STRING).replace("+", Constants.EMPTY_STRING).replace("(", Constants.EMPTY_STRING).replace(")", Constants.EMPTY_STRING).trim();
                                        if (phoneNo.length() < 10) {
                                            continue;
                                        }

                                        if (phoneNo.length() == 10) {
                                            phoneNo = phoneNo;
                                        }
                                        contact.MobileNumber = phoneNo;
                                        contact.F5 = "ANDROID";
                                        contact.F6 = deviceId;
                                    }
                                    pCur.close();
                                }
                                if (!Utility.IsNullOrEmpty(contact.MobileNumber)) {
                                    listOfContacts.add(contact);
                                    mydb.addContact(contact);
                                }
                            }
                        }else{
                            Log.d(TAG, "run: Naveen closed");
                        }
                        if (cur != null) {
                            cur.close();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "run: " + ex.getMessage());
                    }
                }

            }).start();
        } catch (Exception ex) {
            Log.e("getAllContacts: ", ex.getMessage());
        }

        return listOfContacts;
    }
}