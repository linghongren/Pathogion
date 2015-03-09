package com.example.ugri.pathogion;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

    Database database = new Database(this);


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
                float newAccuracy = location.getAccuracy();
                long newTime = location.getTime();
                //insert to the database
                log.i("change Location", "start insert");
                database.insertData(newLatitude, newLongitude, newAccuracy, newTime);
            }

            public void onStatusChanged (String provider, int status, Bundle extras) {};

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        log.i("service", "checkLocation");
        mLocationManager.requestLocationUpdates (LocationManager.NETWORK_PROVIDER, 5000, 3, mLocationListener);
    }

}
