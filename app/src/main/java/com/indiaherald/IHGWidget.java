package com.indiaherald;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import static android.content.ContentValues.TAG;

/**
 * Implementation of App Widget functionality.
 */
public class IHGWidget extends AppWidgetProvider {
    public static RemoteViews updateViews;
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ihg_widget);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /// <summary>
    /// This will update the widget list view
    /// </summary>
    /// <param name="context">UI context</param>
    /// <param name="appWidgetId">app widget Id</param>
    /// <returns>returns the view</returns>
    public RemoteViews updateWidgetListView(Context context, int appWidgetId)
    {
        updateViews = new RemoteViews(context.getPackageName(), R.layout.ihg_widget);
        try
        {
            String PACKAGE_NAME = context.getPackageName();
            Intent svcIntent = new Intent(context,WidgetService.class);
            svcIntent.setPackage(PACKAGE_NAME);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            //svcIntent.setData(Android.Net.Uri.Parse(svcIntent.toUri(intentUri.AndroidAppScheme)));
            updateViews.setEmptyView(R.id.listViewWidget, R.id.empty_view);
            updateViews.setRemoteAdapter(R.id.listViewWidget, svcIntent);
        }
        catch (Exception ex)
        {
            Log.e(TAG, "WidgetProvider updateWidgetListView " + ex.getMessage());
        }
        return updateViews;
    }
    public static RemoteViews buildUpdate(Context context)
    {

        updateViews = new RemoteViews(context.getPackageName(), R.layout.ihg_widget);

        Intent configIntent = new Intent(context, MainActivity.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);

        //Click event for LinearLayout lessonRoot
        updateViews.setOnClickPendingIntent(R.id.widgetRoot, configPendingIntent);

        return updateViews;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        int N = appWidgetIds.length;
        for (int i = 0; i < N; i++){
            updateViews = updateWidgetListView(context, appWidgetIds[i]);
            Intent startActivityIntent = new Intent(context, MainActivity.class);
            //startActivityIntent.PutExtra(Constants.WidgetItem, "Hello");
            PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            updateViews.setPendingIntentTemplate(R.id.listViewWidget, startActivityPendingIntent);
            appWidgetManager.updateAppWidget(appWidgetIds[i], updateViews);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i], R.id.listViewWidget);
            //updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}