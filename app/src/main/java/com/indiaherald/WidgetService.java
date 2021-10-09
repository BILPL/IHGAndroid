package com.indiaherald;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViewsService;

public class WidgetService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        ListProvider lp = new ListProvider(this.getApplicationContext());

        return lp;
    }
}