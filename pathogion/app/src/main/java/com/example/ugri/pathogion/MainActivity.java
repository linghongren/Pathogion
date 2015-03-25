package com.example.ugri.pathogion;

import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements
        OnMapReadyCallback {

    Database db = new Database (this);
    GoogleMap map;

    Log log;
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

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(new UserLocation(), "Location Update");
        fragmentTransaction.commit();

    }


    @Override
    public void onMapReady(GoogleMap mMap) {
 //       startService(intent_userLocation);

        map = mMap;
        LatLng originLoc = new LatLng(40.7142700, -74.0059700);
        map.setMyLocationEnabled(true);

        //move Camera to focus on one thing
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(originLoc, (float)15.0));
  //      log.i("main", "onMapReady");

    }

    //get user's locations and time from an existing database.
    //store them into lists
    public void mapUserLocation (View view){
        List<LatLng> userLoc = new ArrayList<>();

        if ((db.initializeForDataQuery())) {

            while (db.afterOneDataQuery()) {
                userLoc.add(db.passLatLng());
            }

            int size = userLoc.size();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc.get(size - 1), (float) 15.0));

            //add polyline
            Polyline polyline = map.addPolyline(new PolylineOptions().geodesic(true));
            polyline.setColor(Color.BLACK);
            polyline.setWidth(20);

            polyline.setPoints(userLoc);
        }
        else
            log.i ("main", "no database");
    }

    //a button
    public void clearDb(View view){
        log.i(" clearDb", "clear");
        this.deleteDatabase("userLocationsData");
    }


}

