package com.indiaherald;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.androidbrowserhelper.trusted.LauncherActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.os.Handler;
import android.view.View;
import android.widget.Toast;

public class DashboardActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            Utility.SyncContactsToServer(this);
        } else {
            SplashActivity.splashActivity.requestPermission();
        }
        Intent intent1 = this.getIntent();
        if( null !=intent1 && null != intent1.getData() && null != intent1.getData().getHost()){
            String urlscheme = intent1.getData().getHost();
            //Toast.makeText(this, urlscheme + " Host called", Toast.LENGTH_LONG).show();
            if(urlscheme.toLowerCase().contains("lang=")){
                String language = urlscheme.split("lang=")[1];
                Utility.SetUserPreferences(Constants.ChoosenLanguage, language, this);
                ListProvider.populateListItem();
            }else{
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, urlscheme + " hello");
                this.startActivity(intent);
            }

        }
        this.finish();
    }
}