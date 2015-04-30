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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;




public class Database extends SQLiteOpenHelper {

    Log log;
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "userLocationsData";

    SQLiteDatabase db;
    Cursor cursor;

    String dateFormat = "yyyy-MM-dd";
    SimpleDateFormat formatDate = new SimpleDateFormat(dateFormat);

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


    //passing a list of latlng on a given date
    public List<LatLng> passLatLngDate (String dt){

        log.i("database", "passLatLngDate");
        List <LatLng> dLoc = new ArrayList<>();

        Date dDt;

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        //convert string to date, because date objects can be compared using Calendar objects.
        try{
            dDt = formatDate.parse(dt);
            cal1.setTime( dDt );
        }catch(ParseException e) {
            e.printStackTrace();
        }

        if (initializeForDataQuery()){
            while (afterOneDataQuery()){
                long temp = passTime();
//                log.i ("database", String.valueOf(temp));
                cal2.setTimeInMillis(temp);
               //add new date to the list
                if ((cal1.get(Calendar.YEAR))==(cal2.get(Calendar.YEAR)) &&
                            (cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR))){

                    //if a location is on the date we are looking for, save it
                    dLoc.add(passLatLng());
                    log.i("database", String.valueOf(dLoc.size()));
                }

                //if cal2 is a day after cal1, there is no need to compare.
                if ((cal1.get(Calendar.YEAR))>cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.DAY_OF_YEAR) > cal2.get(Calendar.DAY_OF_YEAR))
                    break;
            }

        }

        return dLoc;
    }


    //return a list with all the dates in the database
    public List<String> existingDates (){
        List<String> date = new ArrayList<>();

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();


        if (initializeForDataQuery()){
            while (afterOneDataQuery()){
                long temp = passTime();
//                log.i ("database", String.valueOf(temp));
                cal1.setTimeInMillis(temp);
                //add new date to the list
                if (!date.isEmpty()){
                    if (! ((cal1.get(Calendar.YEAR))==(cal2.get(Calendar.YEAR)) &&
                            (cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)))){

                        //check the year, and day of year.
                        //if it is a different date, save it.
                        Date dt = new Date();
                        dt.setTime(temp);
                        date.add(formatDate.format(dt));
                        cal2.setTimeInMillis(temp);
                    }

                }
                else{ //add the first date to the list
                    Date dt = new Date();
                    dt.setTime(temp);
                    date.add(formatDate.format(dt));
                    cal2.setTimeInMillis(temp);
                }


            }
        }

        return date;
    }

}