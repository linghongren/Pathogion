package com.example.ugri.pathogion;

/**
 * Map fragment that handles mapping
 */

import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class Map extends Fragment implements
        OnMapReadyCallback {

    GoogleMap map;
    List<LocationStruct> locU = new ArrayList();
    List<LocationStruct> locP = new ArrayList<>();
    List<LatLng> effectedPoints = new ArrayList<>();
    List<LatLng> intersectionPoints = new ArrayList<>();

    List<Marker> markerP = new ArrayList<>();
    List<Polyline> polyU = new ArrayList<>();
    Polyline polyP;
    List<Circle> effectArea = new ArrayList<>();

    static int ESTIMATED_DISTANCE = 100;


    boolean isPre = false;

    Log log;
//    static final String dateFormat = "yyyy-MM-dd HH:mm:ss";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //code map to the app

        log.i("map", "oncreate");
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


    public void executeAll(){

        locU = (((MainActivity)getActivity()).getUserLocations());
        locP = (((MainActivity)getActivity()).getPatientLocations());
        effectedPoints = (((MainActivity)getActivity()).getEffectedPoints());
        intersectionPoints = (((MainActivity)getActivity()).getIntersectionPoints());

        mapUserLocation();
        mapPatientLocation();
        mapIntersection();
        mapEffectedArea();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Polyline poly: polyU) {
            builder.include(poly.getPoints().get(0));
        }
        LatLngBounds bounds = builder.build();
        int padding = 2; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        map.moveCamera(cu);

        map.animateCamera(cu);
    }


    public void mapUserLocation(){
        List <LatLng> loc = new ArrayList<>();

        log.i("map", String.valueOf(locU.size()));

        LatLng temp = locU.get(0).coor;
        Distance preD = new Distance(temp);
        Distance curD = new Distance();
        loc.add(temp);

        int loop = 1;
        while (loop < locU.size()){
            temp = locU.get(loop).coor;
            loc.add(temp);
            curD.setLoc(temp);

            if (curD.findDistance(preD)>ESTIMATED_DISTANCE){
                Polyline pol = map.addPolyline(new PolylineOptions().geodesic(true)
                        .add(loc.get(loop-1), loc.get(loop)));
                pol.setColor(Color.GRAY);
                pol.setWidth(20);

                polyU.add(pol);
            }

            else {
                Polyline pol = map.addPolyline(new PolylineOptions().geodesic(true)
                        .add(loc.get(loop-1), loc.get(loop)));
                pol.setColor(Color.BLACK);
                pol.setWidth(20);

                polyU.add(pol);
            }
            preD.setDistance(curD);

            loop ++;
        }
    }

    public void mapPatientLocation (){
        List <LatLng> loc = new ArrayList<>();
        log.i("map", "size of ls " + String.valueOf(locP.size()));
        int loop = 0;
        while (loop < locP.size()){
            LatLng temp;
            temp = locP.get(loop).coor;
            loc.add(temp);

            Marker marker = map.addMarker(new MarkerOptions()
                        .position(temp)
                        .title(locP.get(loop).time.toString()));

            markerP.add(marker);
            loop++;
        }
        polyP = map.addPolyline(new PolylineOptions().geodesic(true));
        polyP.setColor(Color.RED);
        polyP.setWidth(20);

        polyP.setPoints(loc);
    }

    public void mapEffectedArea(){
        int size = effectedPoints.size();
        log.i("map", "size of effectedPoints " + String.valueOf(size));

        if (size>0){
            for (int i = 0; i < size; i++){
                Circle circle = map.addCircle(new CircleOptions()
                        .center(effectedPoints.get(i))
                        .radius(100)
                        .strokeColor(Color.BLUE)
                        .fillColor(Color.argb(50, 0, 0, 255)));

                effectArea.add(circle);
            }
        }
    }



    public void mapIntersection(){
        log.i("map", "map intersection");
        int size = intersectionPoints.size();

        if (size>0){
            for (int i = 0; i < size; i++){
                Circle circle = map.addCircle(new CircleOptions()
                        .center(intersectionPoints.get(i))
                        .radius(100)
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.argb(50, 0, 255, 0)));

                effectArea.add(circle);
            }
        }
    }


    public void clearMapping(){
        for (int i = 0; i < markerP.size(); i ++){
            markerP.get(i).remove();
        }
        for (int i = 0; i < polyU.size(); i ++){
            polyU.get(i).remove();
        }
        for (int i = 0; i < effectArea.size(); i++){
            effectArea.get(i).remove();
        }
        polyP.remove();
        markerP.clear();
        polyU.clear();
        effectArea.clear();
        effectedPoints.clear();
        intersectionPoints.clear();

        isPre = false;
    }


/*
    //asynctask to find effected Area
    private class effectedArea extends AsyncTask<Void, Void, Void>{

        protected Void doInBackground(Void... params){

            findEffectedPoints();


            return null;
        }

        protected void onPostExecute(Void result) {
            log.i("map", "after the loop add " + String.valueOf(effectedPoints.size()));
            mapEffectedArea();
        }
    }

    private class effectedLine extends AsyncTask <Void, Void, Void>{
        protected Void doInBackground (Void ... params){
            findEffectedIntersectionLine();

            return null;
        }

        protected void onPostExecute (Void result){
            mapIntersection();
        }
    }

*/
}


