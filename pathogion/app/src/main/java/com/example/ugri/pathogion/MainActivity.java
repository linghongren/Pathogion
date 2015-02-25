package com.example.ugri.pathogion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


public class MainActivity extends Activity implements OnMapReadyCallback {
//    Intent intent_userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //code map to the app


        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
 //       intent_userLocation = new Intent(this, UserLocation.class);

    }

    @Override
    public void onMapReady(GoogleMap mMap) {
//        startActivity(intent_userLocation);
        LatLng currentLoc = new LatLng(40.7127, -74.0059);

        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, (float) 15.0));

        //add polyline
 /*       map.addPolyline(new PolylineOptions().geodesic(true)
                .add(new LatLng(-33.866, 151.195))  // Sydney
                .add(new LatLng(-18.142, 178.431))  // Fiji
                .add(new LatLng(21.291, -157.821))  // Hawaii
                .add(new LatLng(37.423, -122.091))  // Mountain View
        );
  */
        //add marker
  /*      map.addMarker(new MarkerOptions()
                .position(currentLoc)
                .title("ME"));
  */
    }
}
