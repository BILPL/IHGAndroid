package com.indiaherald;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

//This class was created for using all the utility methods across different files.
public class Utility {
    public static boolean IsNullOrEmpty(String str) {
        Boolean value = false;
        try {
            if (null == str || str.length() == 0 || str == "" || str.isEmpty()) {
                return true;
            }

        } catch (Exception ex) {

        }
        return value;
    }

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String GetUTCDateTimeWithFormat() {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            final String utcTime = sdf.format(new Date());

            return utcTime;
        } catch (Exception ex) {
            Log.e("GetUTCDateTimeWith: ", ex.getMessage());
            throw ex;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        boolean outcome = false;

        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo[] networkInfos = cm.getAllNetworkInfo();

            for (NetworkInfo tempNetworkInfo : networkInfos) {


                /**
                 * Can also check if the user is in roaming
                 */
                if (tempNetworkInfo.isConnected()) {
                    outcome = true;
                    break;
                }
            }
        }

        return outcome;
    }
    public static Date ConvertStringToDate(String StrDate) {
        Date dateToReturn = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        try {
            dateToReturn = (Date) dateFormat.parse(StrDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateToReturn;
    }

    /// <summary>
    /// Sets the given value in user preferences using given key.
    /// </summary>
    /// <returns><c>true</c>, if user preferences was set, <c>false</c> otherwise.</returns>
    /// <param name="key">Preference key.</param>
    /// <param name="value">Preference value.</param>
    public static boolean SetUserPreferences(String key, String value, Context context) {
        try {
            SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
            sprefs.edit().putString(key, value).commit();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /// <summary>
    /// This method for Get the stores the data from shared preferences based on key value
    /// </summary>
    /// <param name="key">store key value</param>
    /// <returns>it return's stored string</returns>
    public static String GetUserPreference(String key, Context context) {
        try {
            SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);

            return sprefs.getString(key, Constants.EMPTY_STRING);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /// <summary>
    /// Gets device unique id.
    /// </summary>
    /// <param name="context">Activity context.</param>
    /// <returns>Device unique id.</returns>
    public static String GetDeviceUniqueId(Context context) {
        String value = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return value;
    }

    //With the help of this method we are registering device id and token with the server
    public static void RegisterOrUpdateDeviceToken(Context context) {
        final Boolean result = false;
        String deviceUniqueId = GetDeviceUniqueId(context);
        String deviceToken = GetUserPreference(Constants.DeviceToken, context);
        final String endPoint = "https://www.indiaherald.com/" + "api/ApheraldBaseApi/RegisterOrUpdateDeviceToken?deviceType=ANDROID" + "&deviceUniqueId=" + deviceUniqueId + "&deviceToken=" + deviceToken;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    HttpURLConnection connection = null;
                    URL url = new URL(endPoint);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("GET"); // hear you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                    connection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`"application/octet-stream"
                    connection.connect();

                    int response = connection.getResponseCode();
                    if (response >= 200 && response <= 399) {
                        Utility.SetUserPreferences(Constants.DeviceUpdated, "true", context);
                    } else {

                    }

                } catch (Exception ex) {
                    Log.e(TAG, "RegisterOrUpdateDeviceToken: ");
                }
            }

        }).start();
    }

    //This boolean lets you know is syncing already going on
    public static Boolean isContactServiceCallGoingOn = false;

    //With this method we are syncing contacts to server
    public static void SyncContactsToServer(final Activity activity) {
        try {
            String lastSyncDate = Utility.GetUserPreference(Constants.ContactsLastSyncDateTime, activity);
            if (!IsNullOrEmpty(lastSyncDate)) {
                long differenceinTime = (ConvertStringToDate(GetUTCDateTimeWithFormat()).getTime() - Utility.ConvertStringToDate(lastSyncDate).getTime()) / (1000 * 60 * 60) % 24;
                Log.d(TAG, "SyncContactsToServer: " + differenceinTime);
                if (differenceinTime < 3)
                    return;
            }

            MainActivity.mainActivity.ReadAndContacts();
            final List<User> finalcontacts = MainActivity.mainActivity.mydb.getAllContacts();
            if (finalcontacts == null || finalcontacts.size() <= 0) {
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String UserId ="168EE324-C381-41C2-AE4F-0027AE23DB9A";
                        if (Utility.IsNullOrEmpty(UserId))
                            return;
                        String endPoint = "https://www.indiaherald.com/api/APHeraldBaseApi/SyncContacts?userId=" + UserId;

                        HttpURLConnection connection = null;
                        URL url = new URL(endPoint);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoOutput(true);
                        connection.setRequestMethod("POST"); // hear you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                        connection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`"application/octet-stream"
                        Gson gson = new Gson();
                        String content = gson.toJson(finalcontacts);

                        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                        wr.write(content.getBytes());
                        connection.connect();
                        InputStream is;
                        String resp = connection.getResponseMessage();
                        int response = connection.getResponseCode();
                        if (response >= 200 && response <= 399) {
                            Utility.SetUserPreferences(Constants.ContactsLastSyncDateTime, GetUTCDateTimeWithFormat(), activity);
                            wr.flush();
                            wr.close();
                            //return is = connection.getInputStream();
                            return;
                        } else {
                            wr.flush();
                            wr.close();
                            //return is = connection.getErrorStream();
                            return;
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "run: " + ex.getMessage());
                    } finally {
                        isContactServiceCallGoingOn = false;
                    }
                }
            }).start();

        } catch (Exception ex) {
            Log.e("SyncContactsToServer: ", ex.getMessage());
        }
    }
}

