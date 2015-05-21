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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
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
    static int EFFECTED_RADIUS = 100;

    boolean isPre = false;

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

    public void copyLocations (List<LocationStruct> userLoc, List<LocationStruct> patientLoc){
        if (userLoc.size()!=0 && patientLoc.size()!=0){
            log.i ("map", " copyLocation");
            locU = userLoc;
            locP = patientLoc;

            if (isPre)
                clearMapping();

            new effectedLine().execute();
            new effectedArea().execute();

            mapLocations();

            isPre = true;
        }
        else if (isPre){
            log.i("map", "clear mapping");
            clearMapping();

            isPre = false;
        }
    }

    //map both userLoc(black) and patientLoc(red) on the map.
    public void mapLocations (){

        if (locP.size()>0){
            log.i("map", "map patient location" + String.valueOf(locP.size()));
            int size = locP.size();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(locP.get(size - 1).coor, (float) 12.0));

            mapPatientLocation();

        }
        if (locU.size()>0){
            log.i("map", "map userLocation " + String.valueOf(locU.size()));
            int size = locU.size();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(locU.get(size - 1).coor, (float) 12.0));

            mapUserLocation();
        }

    }

    public void mapUserLocation(){
        List <LatLng> loc = new ArrayList<>();

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

    //compare points and find effected points.
    public void findEffectedPoints(){
        int uSize = locU.size();
        int pSize = locP.size();
        log.i("map", String.valueOf(locU.size()) + " " + String.valueOf(pSize));

        for (int i = 0; i < pSize; i++){
            Distance dis1 = new Distance(locP.get(i).coor);
            Time time1 = new Time(locP.get(i).time);

            for (int j = 0; j < uSize; j++){
                Time time2 = new Time(locU.get(j).time);
                //if a user's time is five minutes after the patient's time
                if (time2.timeAfter(time1.getTimeC())){
//                    log.i("map", "break");
                    break;
                }

//                log.i("map", time1.getTimeD().toString() + " " + time2.getTimeD().toString());
                if (time1.closeOnTime(time2.getTimeC())){
                    Distance dis2 = new Distance(locU.get(j).coor);
                    if (dis1.findDistance(dis2) < EFFECTED_RADIUS){
                        if (effectedPoints.size()>0) {
                            int s = effectedPoints.size();
                            if ((effectedPoints.get(s-1).latitude != dis2.getLatLng().latitude)
                                    && (effectedPoints.get(s - 1).longitude != dis2.getLatLng().longitude))
                                effectedPoints.add(locP.get(i).coor);
                        }
                        else{
                            effectedPoints.add(locP.get(i).coor);
                        }
                    }
                }
            }
        }
    }

    public void findEffectedIntersectionLine(){
        log.i("map", "find intersection line");
        int uSize = locU.size();
        int pSize = locP.size();

        if (!(uSize > 0 && pSize> 0)){
            return;
        }

        List <PointStruct> eqUser1 = new ArrayList<>();
        List <PointStruct> eqPatient2 = new ArrayList<>();
        eqUser1 = findEquations(locU);
        eqPatient2 = findEquations(locP);

        //these sizes should be one less than its corresponding LocationStruct sizes.
        int uEq1 = eqUser1.size();
        int pEq2 = eqPatient2.size();

        for (int i = 0; i < uEq1; i ++){
            LatLng userTemp1 = locU.get(i).coor;
            LatLng userTemp2 = locU.get(i+1).coor;
/*            Time uTime1 = new Time(locU.get(i).time);
            Time uTime2 = new Time(locU.get(i+1).time);

*/
            double coef = eqUser1.get(i).coefficient;
            double intercept = eqUser1.get(i).yIntercept;
//            log.i("map", "find line " + String.valueOf(coef) + " " + String.valueOf(intercept));
            for (int j = 0; j < pEq2; j++){
/*                Time pTime1 = new Time(locP.get(i).time);
                Time pTime2 = new Time(locP.get(i+1).time);

                //uTime and pTime do not intersect
                if (!((uTime1.timeBetween(pTime1.getTimeC(), pTime2.getTimeC()))
                    || (uTime2.timeBetween(pTime1.getTimeC(), pTime2.getTimeC()))
                        || (pTime1.timeBetween(uTime1.getTimeC(), uTime2.getTimeC()))
                            || (pTime2.timeBetween(uTime1.getTimeC(), uTime2.getTimeC())))){
                    continue;
                }
*/
                double x = (eqPatient2.get(j).yIntercept - intercept)/
                        (coef - eqPatient2.get(j).coefficient);

//                log.i("map", "get double " +String.valueOf(x));
                if (Double.isInfinite(x))

                    x = (double) 0;

 //               log.i("map", "longitudes "+ String.valueOf(userTemp1.latitude) + " " + String.valueOf(userTemp2.latitude));
                //if the intersection point is between user's points
                if ( (x < userTemp1.latitude && x > userTemp2.latitude)
                        || (x <userTemp2.latitude && x> userTemp1.latitude)){
                    double y = (coef * x) + intercept;
//                    log.i("map", "get double y " +String.valueOf(y));

//                    log.i("map", "longitudes "+ String.valueOf(userTemp1.longitude) + " " + String.valueOf(userTemp2.longitude));
                    if ((y <userTemp1.longitude && y > userTemp2.longitude)
                        || (y < userTemp2.longitude && y > userTemp1.longitude)){
                        intersectionPoints.add(new LatLng(x,y));
                    }
                }
            }
        }
        log.i("map", "size of intersection points " + String.valueOf(intersectionPoints.size()));
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
    public List<PointStruct> findEquations(List<LocationStruct> ls){
        log.i("map", "find equations");
        List<PointStruct> eq = new ArrayList<>();
        int lsSize = ls.size();
        if (lsSize >0) {
            for (int i = 1; i < lsSize; i++) {
                PointStruct pt = new PointStruct();
                //(y2 - y1 )/ (x2 - x1)
                pt.coefficient = (ls.get(i).coor.latitude - ls.get(i - 1).coor.latitude)/
                            (ls.get(i).coor.longitude-ls.get(i-1).coor.longitude);

                if (Double.isNaN(pt.coefficient))
                    pt.coefficient = (double) 0;

                pt.yIntercept = ls.get(i - 1).coor.longitude - pt.coefficient * ls.get(i-1).coor.latitude;

//                log.i("map", "coef " + String.valueOf(pt.coefficient) + " " + String.valueOf(pt.yIntercept));
                eq.add(pt);
            }
        }
        return eq;
    }

    private class PointStruct{
        double coefficient;
        double yIntercept;
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
    }



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


}


