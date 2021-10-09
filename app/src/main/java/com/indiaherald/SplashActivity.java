package com.indiaherald;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import android.widget.Button;
import android.view.View;

public class SplashActivity extends AppCompatActivity {
    public static SplashActivity splashActivity;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int REQUEST_READ_CONTACTS = 79;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splashActivity=this;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(splashActivity, Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {
                    Utility.SyncContactsToServer(splashActivity);
                } else {
                    requestPermission();
                }
            }
        });


    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        try {

            if (requestCode == REQUEST_READ_CONTACTS) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (ActivityCompat.checkSelfPermission(splashActivity, Manifest.permission.READ_CONTACTS)
                                == PackageManager.PERMISSION_GRANTED) {
                            Utility.SyncContactsToServer(splashActivity);
                        } else {
                            requestPermission();
                        }
                    }
                });

            }

        }catch (Exception ex){

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Utility.SyncContactsToServer(this);
                    MainActivity.mainActivity.StartPWA(this);
                }else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                    MainActivity.mainActivity.StartPWA(this);
                }

                return;
            }

        }
    }


    public void requestPermission() {
        try {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                // show UI part if you want here to show some rationale !!!
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_READ_CONTACTS);
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    this.requestPermissions(new String[]{
                            Manifest.permission.READ_CONTACTS,

                    }, REQUEST_READ_CONTACTS);
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_READ_CONTACTS);
            }

            // StartPWA();
        } catch (Exception ex) {
            Log.e("requestPermission: ", ex.getMessage());
        }

    }

}