package com.example.ugri.pathogion;

/**
 * Map fragment that handles mapping
 */

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;


public class Map extends Fragment implements
        OnMapReadyCallback {

    GoogleMap map;

    Log log;
//    static final String dateFormat = "yyyy-MM-dd HH:mm:ss";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //code map to the app

        log.i("map", "map oncreate");
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap mMap) {

        log.i("map", "on Map Ready");
        map = mMap;
        LatLng originLoc = new LatLng(40.7142700, -74.0059700); // a randon latlng in New York City
        map.setMyLocationEnabled(true);

        //move Camera to focus on one thing
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(originLoc, (float) 15.0));
    }


    //map both userLoc(black) and patientLoc(red) on the map.
    public void mapUserLocation (List<LatLng> userLoc, List<LatLng> patientLoc){
        if (userLoc.size()>0){
            log.i("map", "map userLocation " + String.valueOf(userLoc.size()));
            int size = userLoc.size();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc.get(size - 1), (float) 10.0));

            //add polyline
            Polyline polylineUser = map.addPolyline(new PolylineOptions().geodesic(true));
            polylineUser.setColor(Color.BLACK);
            polylineUser.setWidth(20);

            polylineUser.setPoints(userLoc);

        }

        if (patientLoc.size()>0){
            log.i("map", "map patient location");
            int size = patientLoc.size();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(patientLoc.get(size - 1), (float) 10.0));

            //add polyline
            Polyline polylinePatient = map.addPolyline(new PolylineOptions().geodesic(true));
            polylinePatient.setColor(Color.RED);
            polylinePatient.setWidth(20);

            polylinePatient.setPoints(patientLoc);
        }

    }


}
