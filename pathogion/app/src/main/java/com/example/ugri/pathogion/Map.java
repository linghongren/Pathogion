package com.example.ugri.pathogion;

/**
 * Map fragment that handles mapping
 */

import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;


public class Map extends Fragment implements
        OnMapReadyCallback {

    GoogleMap map;

    static int ESTIMATED_DISTANCE = 20;

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
    public void mapLocations (List<LocationStruct> userLoc, List<LocationStruct> patientLoc){
        if (userLoc.size()>0){
            log.i("map", "map userLocation " + String.valueOf(userLoc.size()));
            int size = userLoc.size();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc.get(size - 1).coor, (float) 12.0));

            mapUserLocation(userLoc);
        }

        if (patientLoc.size()>0){
            log.i("map", "map patient location" + String.valueOf(patientLoc.size()));
            int size = patientLoc.size();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(patientLoc.get(size - 1).coor, (float) 12.0));

            mapPatientLocation(patientLoc);

        }

    }

    //it is mapping user's locations
    public void mapUserLocation (List<LocationStruct> ls){
        List <LatLng> loc = new ArrayList<>();

        LatLng temp = ls.get(0).coor;
        Distance preD = new Distance(temp);
        Distance curD = new Distance();
        loc.add(temp);

        int loop = 1;
        while (loop < ls.size()){
            temp = ls.get(loop).coor;
            loc.add(temp);
            curD.setLoc(temp);
            if (preD.findDistance(curD) > ESTIMATED_DISTANCE && loc.size()>1){
                //add polyline
                Polyline pol = map.addPolyline(new PolylineOptions().geodesic(true));
                pol.setColor(Color.BLACK);
                pol.setWidth(20);
                pol.setPoints(loc);

                loc.clear();
                loc.add(temp);

                log.i("map","mapuserloc");

                //start finding the estimated distance, make it grey
                while (loop < ls.size()){
                    preD.setDistance(curD);
                    temp = ls.get(loop).coor;
                    loc.add(temp);
                    curD.setLoc(temp);
                    if (preD.findDistance(curD)< ESTIMATED_DISTANCE && loc.size() >1){
                        Polyline pol2 = map.addPolyline(new PolylineOptions().geodesic(true));
                        pol2.setColor(Color.GRAY);
                        pol2.setWidth(20);

                        pol2.setPoints(loc);
                        loc.clear();
                        loc.add(temp);

                        log.i("map","mapuserloc 2");

                        break;

                    }

                    loop ++;
                }
            }
            loop ++;
        }

        if (loc.size() > 1){
            Polyline pol3 = map.addPolyline(new PolylineOptions().geodesic(true));
            pol3.setColor(Color.GRAY);
            pol3.setWidth(20);

            pol3.setPoints(loc);
        }
    }

    public void mapPatientLocation (List<LocationStruct> ls){
        List <LatLng> loc = new ArrayList<>();
        log.i("map", "size of ls " + String.valueOf(ls.size()));
        int loop = 0;
        while (loop < ls.size()){
            LatLng temp;
            temp = ls.get(loop).coor;
            loc.add(temp);
            map.addMarker(new MarkerOptions()
                        .position(temp)
                        .title("P"));

            loop++;
        }
        Polyline polyPatient = map.addPolyline(new PolylineOptions().geodesic(true));
        polyPatient.setColor(Color.RED);
        polyPatient.setWidth(20);

        polyPatient.setPoints(loc);
    }

    public void clearAllMappings(){
        map.clear();
    }
}


