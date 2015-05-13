package com.example.ugri.pathogion;

/**
 * set up SQLite and methods for location storage
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Database extends SQLiteOpenHelper {

    Log log;
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "userLocationsData";


    SQLiteDatabase db;
    Cursor cursor;

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


    //constructor
    Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE);
//        log.i("database", "oncreate");
    }

    //insert data into the database with four values
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

    //initializing the cursor and database for reading.
    //return true if the database is not empty
    public boolean initializeForDataQuery () {
        log.i("Database", "getDatabaseInformation");
        db = getReadableDatabase();

        log.i("Database", "get readable database");
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

    //return true if more dataquery can be done, cursor points to the next row in db
    public boolean afterOneDataQuery(){
        return cursor.moveToNext();
    }


    //pass lat and lng that the current cursor points.
    public LatLng passLatLng (){
        double lat = cursor.getDouble(0);
        double longi = cursor.getDouble(1);
     //   float accuracy = cursor.getFloat(2);
        LatLng loc = new LatLng(lat, longi);
     //   loc.setAccuracy(accuracy);

        return loc;
    }

    //pass the time that the current cursor points
    public long passTime () {
        long time = cursor.getLong(3);
        return time;
    }


    //passing a list of locationStruct on a given date
    public List<LocationStruct> passLatLngDate (String dt){

        log.i("database", "passLatLngDate");
        List <LocationStruct> dLoc = new ArrayList<>();

        Time wantedDate = new Time (dt, 1);

        if (initializeForDataQuery()){
            while (afterOneDataQuery()){
                long temp = passTime();
//                log.i ("database", String.valueOf(temp));
                Time dataDate = new Time (temp);
               //add new date to the list
                if (dataDate.onSameDay(wantedDate.getTimeC())){
                    //if a location is on the date we are looking for, save it
                    LocationStruct local = new LocationStruct();
                    local.time = new Date(temp);
                    local.coor = passLatLng();
                    dLoc.add(local);
//                    log.i("database", String.valueOf(dLoc.size()));
                }

                //if the date in the database is after the wantedDate, there is no need to compare.
                if (dataDate.dayAfter(wantedDate.getTimeC()))
                    break;
            }

        }
        return dLoc;
    }


    //return a list with all the dates in the database
    public List<String> existingDates (){
        List<String> date = new ArrayList<>();

        Time time1 = new Time();
        Time time2 = new Time();

        if (initializeForDataQuery()){
            while (afterOneDataQuery()){
                long temp = passTime();
 //               log.i ("database", String.valueOf(temp));
                time1.setTime(temp);
                //add new date to the list
                if (!date.isEmpty()){
                    if (! time1.onSameDay(time2.getTimeC())){
                        //check the year, and day of year.
                        //if it is a different date, save it.
                        date.add(time1.getTimeS());
                        time2.setTime(temp);
                    }

                }
                else{ //add the first date to the list
                    time2.setTime(temp);
                    date.add(time2.getTimeS());
                }

            }
        }

        return date;
    }

}