package com.indiaherald;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "contactManager";
    // contact table name
    private static final String TABLE_CONTACTS = "contacts";
    // contacts table column names
    private static final String KEY_NAME = "name";
    private static final String KEY_PH_NO = "phone_number";
    private static final String KEY_ID = "id";
    private static final String KEY_DEVICETYPE = "devicetype";
    private static final String KEY_DEVICEID = "deviceid";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String CREATE_CONACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," +  KEY_DEVICETYPE + " TEXT,"
                +  KEY_DEVICEID + " TEXT,"
                + KEY_PH_NO + " TEXT NOT NULL UNIQUE" + ")";
        db.execSQL(CREATE_CONACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL( "DROP TABLE IF EXISTS'" + TABLE_CONTACTS + "'");
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    public void addContact(User contact) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_NAME, contact.Name); // Contact Name
            values.put(KEY_DEVICETYPE, contact.F5);
            values.put(KEY_DEVICEID, contact.F6);
            values.put(KEY_PH_NO, contact.MobileNumber); // Contact Phone
            // Inserting Row
            db.insert(TABLE_CONTACTS, null, values );
            db.close(); // Closing database connection
        }catch(Exception ex){
            Log.e("getAllContacts: ", ex.getMessage() );
        }


    }
    // Getting All Contacts
    public List<User> getAllContacts() {
        List<User> contactList = new ArrayList<User>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                User contact = new User();
                //contact.Email = cursor.getString(0) ;
                contact.Name = cursor.getString(1);
                contact.MobileNumber = cursor.getString(4);
                contact.F5 = cursor.getString(2);
                contact.F6 = cursor.getString(3);
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

//    // Updating single contact
//    public int updateContact(ContactItems contact) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_NAME, contact.getName());
//        values.put(KEY_PH_NO, contact.getNumber());
//
//        // updating row
//        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
//                new String[] { String.valueOf(contact.getType()) });
//    }

    // Deleting single contact
//    public void deleteContact(ContactItems contact) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
//                new String[] { String.valueOf(contact.getType()) });
//        db.close();
//    }

    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

}
