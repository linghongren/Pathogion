package com.example.ugri.pathogion;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements OnMapReadyCallback {
    Intent intent_userLocation;
    Database db = new Database (this);
    GoogleMap map;
//    Log log;
    List<LatLng> userLoc = new ArrayList<LatLng>();
    static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //code map to the app

  //      log.i("start", "main oncreate");
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        intent_userLocation = new Intent(this, UserLocation.class);

    }

    @Override
    public void onPause(){
        super.onPause();
 //       log.i("onPause", "start pause");
    }

    @Override
    public void onStop(){
        super.onStop();
 //       log.i("onStop", "start stop");
        startService(intent_userLocation);
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
        startService(intent_userLocation);

        map = mMap;

        LatLng originLoc = new LatLng(40.7142700, -74.0059700);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLoc, (float)15.0));


  //      log.i("main", "onMapReady");

    }


    //get user's locations and time from an existing database.
    //store them into lists
    public void mapUserLocation (View view){
        //stop service first, to prevent reading from endless data
        stopService (intent_userLocation);
        db.initializeForDataQuery();

        while(db.afterFirstDataQuery()){
            Location oneLoc = db.passLocation();
            LatLng latlng = new LatLng(oneLoc.getLatitude(),oneLoc.getLongitude());
            userLoc.add(latlng);

            /* output time from long into dateFormat
            long time = oneLoc.getTime();
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            sdf.format(date);
            */

        }

  //      log.i("mapUserLocation", "finishedAllDataQuery");
        startService(intent_userLocation);

        int size = userLoc.size();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc.get(size-1), (float)15.0));

        //add polyline
        Polyline polyline = map.addPolyline(new PolylineOptions().geodesic(true));
        polyline.setColor(Color.BLACK);
        polyline.setWidth(50);

        polyline.setPoints(userLoc);

    }

    public void clearDb(View view){
        stopService(intent_userLocation);
        this.deleteDatabase("userLocationsData");
        startService(intent_userLocation);

    }

}

