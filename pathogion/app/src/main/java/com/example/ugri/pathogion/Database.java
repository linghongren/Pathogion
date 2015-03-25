package com.example.ugri.pathogion;

/**
 * Created by UGRI on 3/4/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;


//set up SQLite and methods
public class Database extends SQLiteOpenHelper {

    Log log;
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "userLocationsData";

    SQLiteDatabase db;
    Cursor cursor;

    Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String TABLE_NAME = "Location";

    private class DataStruct {
        static final String CREATE_LAT = "latitude";
        static final String CREATE_LONG = "longitude";
        static final String CREATE_ACCURACY = "accuracy";
        static final String CREATE_TIME = "time";

    }
    //for upgrade, delete every existing data
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + DataStruct.CREATE_LAT + " LATITUDE," + DataStruct.CREATE_LONG + " LONGITUDE, "
            + DataStruct.CREATE_ACCURACY + " ACCURACY," + DataStruct.CREATE_TIME + " TIME)";

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE);
//        log.i("database", "oncreate");
    }

    public void insertData(double lat, double longitude, Float accuracy, long time){

        long row = 0;
        db = getWritableDatabase();

        ContentValues value = new ContentValues ();
        value.put (DataStruct.CREATE_LAT, lat);
        value.put (DataStruct.CREATE_LONG, longitude);
        value.put (DataStruct.CREATE_ACCURACY, accuracy);
        value.put (DataStruct.CREATE_TIME, time);
        row = db.insert(TABLE_NAME, null, value);

        log.i("row number", String.valueOf(row));
        db.close();
    }


    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion){
        //delete old SQL and create a new one
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public boolean initializeForDataQuery () {
 //       log.i("Database", "getDatabaseInformation");
        db = getReadableDatabase();

 //       log.i("Database", "get readable database");
        if (db == null)
            return false;

        cursor = db.rawQuery("select * from Location", null);
        cursor.moveToFirst();
        //       log.i ("database", "cursor initialized");

        int n = cursor.getCount();
        log.i("cursor count", String.valueOf(n));

        if (n < 1)
            return false;

        return true;

    }

    //pass location to the current cursor.
    public Location passLocation (){
        double lat = cursor.getDouble(0);
        double longi = cursor.getDouble(1);
        float accuracy = cursor.getFloat(2);
        long t = cursor.getLong(3);
        Location loc = new Location("Initialize");
        loc.setLatitude(lat);
        loc.setLongitude(longi);
        loc.setAccuracy(accuracy);
        loc.setTime(t);

        return loc;
    }

    //return true if more dataquery can be done, cursor points to the next row in db
    public boolean afterFirstDataQuery(){
        return cursor.moveToNext();
    }


}