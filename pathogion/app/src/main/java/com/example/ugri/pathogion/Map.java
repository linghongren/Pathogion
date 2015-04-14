package com.example.ugri.pathogion;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by UGRI on 4/1/15.
 */
public class Map extends Fragment implements
        OnMapReadyCallback {

    Database db = new Database (getActivity());
    GoogleMap map;


    Log log;
    static final String dateFormat = "yyyy-MM-dd HH:mm:ss";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getActivity().setContentView(R.layout.map);
        //code map to the app

        log.i("map", "map oncreate");
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap mMap) {
        //       startService(intent_userLocation);

        map = mMap;
        LatLng originLoc = new LatLng(40.7142700, -74.0059700);
        map.setMyLocationEnabled(true);

        //move Camera to focus on one thing
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(originLoc, (float) 15.0));
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
            log.i ("map", "no database");
    }



}
