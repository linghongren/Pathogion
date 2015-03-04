package com.example.ugri.pathogion;

/**
 * Created by UGRI on 2/24/15.
 */

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;



/**
 * Created by UGRI on 2/18/15.
 */

public class UserLocation extends Service {

    Log log;

    UserDataOpenHelper database = new UserDataOpenHelper(this);


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkLocation ();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }


    //location listen
    public void checkLocation(){
        LocationManager mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener mLocationListener = new LocationListener (){
            public void onLocationChanged (Location location){
                double newLatitude = location.getLatitude();
                double newLongitude = location.getLongitude();
                long newTime = location.getTime();
                //insert to the database
                database.insertData(newLatitude, newLongitude, newTime);
            }

            public void onStatusChanged (String provider, int status, Bundle extras) {};

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        mLocationManager.requestLocationUpdates (LocationManager.NETWORK_PROVIDER, 3000, 3, mLocationListener);
    }

    //set up SQLite and methods
    public class UserDataOpenHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 3;
        private static final String DATABASE_NAME = "userLocationsData";
        SQLiteDatabase db;
        UserDataOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        private static final String TABLE_NAME = "Location";

        private class DataStruct {
            static final String CREATE_LAT = "latitude";
            static final String CREATE_LONG = "longitude";
            static final String CREATE_TIME = "time";

        }
        //for upgrade, delete every existing data
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + DataStruct.CREATE_LAT + " LATITUDE," + DataStruct.CREATE_LONG + " LONGITUDE, "
                + DataStruct.CREATE_TIME + " TIME)";

        @Override
        public void onCreate(SQLiteDatabase db) {
            log.i("onCreate", "called on");
            db.execSQL(CREATE_TABLE);
        }

        public void insertData(double lat, double longitude, long time){

            long row = 0;

            db = getWritableDatabase();

            ContentValues value = new ContentValues ();
            value.put (DataStruct.CREATE_LAT, lat);
            value.put (DataStruct.CREATE_LONG, longitude);
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



    }
}
