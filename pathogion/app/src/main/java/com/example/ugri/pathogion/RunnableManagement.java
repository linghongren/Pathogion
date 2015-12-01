package com.example.ugri.pathogion;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A runnable management class that controls all threads that handle calculations
 */
public class RunnableManagement {
    Log log;

    List<LocationStruct> userLocations = new ArrayList();   //array with user's locations
    List<LocationStruct> patientLocations = new ArrayList<>(); //array with matched patient's locations.
    List<LatLng> effectedPoints = new ArrayList<>();
    List<LatLng> intersectionPoints = new ArrayList<>();

    static int EFFECTED_RADIUS = 100;

    String date;
    Database db;
    int uSize = 0;
    int pSize = 0;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final BlockingDeque<Runnable> mRunnableQueue = new LinkedBlockingDeque<Runnable>();

    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    // Creates a thread pool manager
    ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(
            NUMBER_OF_CORES,       // Initial pool size
            NUMBER_OF_CORES,       // Max pool size
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            mRunnableQueue);

    RunnableManagement(){
        log.i("rmgnt", "constructor");
        //a handler that attaches to the UI thread

    }

    void setRunnableManagement(Context context, String dt) {
        db = new Database(context);
        date = dt;
    }


    /**
     * runnable to fetch userLocation initialize by runnable management
     */
    Runnable fetchUserLocation = new Runnable() {
        public void run() {
            db.getLatLngGivenDate(date);

            userLocations = db.passLatLngDate();

        }
    };

    /**
     * runnable to fetch patientlocation initialize by runnable management
     */

    Runnable fetchPatientLocation = new Runnable() {
        public void run() {

            PatientTrack pt = new PatientTrack(date);

            pt.patientLookUp();

            patientLocations = pt.getPatientLocations();

        }
    };

    boolean executeAll(){

        log.i("rmgnt", "executeall 1");
        mThreadPool.execute(fetchUserLocation);
        mThreadPool.execute(fetchPatientLocation);

        while (mThreadPool.getActiveCount()>0) {
        };

        log.i("rmgnt", "executeall 2");
        findEffectedPoints();
        findEffectedIntersectionLine();

        while (mThreadPool.getActiveCount()>0) {
        };


        log.i("rmgnt","userLoc " + String.valueOf(userLocations.size()));
        log.i("rmgnt","patientLoc " + String.valueOf(patientLocations.size()));


        return true;
    }



    //find effected points by comparing two arrays of points
    void findEffectedPoints() {
        uSize = userLocations.size();
        pSize = patientLocations.size();

        if (uSize > 0 && pSize > 0) {

            for (int i = 0; i < pSize; i++){
                CompareLocations cl = new CompareLocations(i);
                mThreadPool.execute(cl);
            }
        }
    }

    //find the intersection points between two paths
    //after all equations are found
    void findEffectedIntersectionLine() {
        for (int i = 0 ; i < userLocations.size()-1; i ++){
            EffectedIntersectionLine effInterLine = new EffectedIntersectionLine(i);

            mThreadPool.execute(effInterLine);
        }

    }

    //compare points and find effected points.
    private class CompareLocations implements Runnable {
        LocationStruct pt;



        CompareLocations(int i) {
            pt = patientLocations.get(i);
        }

        @Override
        public void run() {
            Time time1 = new Time(pt.time);
            Distance dis1 = new Distance(pt.coor);

            for (int i = 0; i < userLocations.size(); i ++) {
                LocationStruct ur = userLocations.get(i);

                Time time2 = new Time(ur.time);
                //if a user's time is not five minutes after the patient's time
                if (!time2.timeAfter(time1.getTimeC())) {

                    //            log.i("map", time1.getTimeD().toString() + " " + time2.getTimeD().toString());
                    if (time1.closeOnTime(time2.getTimeC())) {
                        Distance dis2 = new Distance(ur.coor);
                        if (dis1.findDistance(dis2) < EFFECTED_RADIUS) {
                            if (effectedPoints.size() > 0) {
                                int s = effectedPoints.size();
                                if ((effectedPoints.get(s - 1).latitude != dis2.getLatLng().latitude)
                                        && (effectedPoints.get(s - 1).longitude != dis2.getLatLng().longitude))
                                    effectedPoints.add(pt.coor);
                            } else {
                                effectedPoints.add(pt.coor);
                            }
                        }
                    }
                }
            }


        }
    }

    //look for the effected point of line intersection
    private class EffectedIntersectionLine implements Runnable {
        int userItem;

        EffectedIntersectionLine(int i) {
            userItem = i;
        }

        @Override
        public void run() {

            if (!(uSize > 0 && pSize > 0)) {
                return;
            }

            //these sizes should be one less than its corresponding LocationStruct sizes.

            LatLng userTemp1 = userLocations.get(userItem).coor;
            LatLng userTemp2 = userLocations.get(userItem + 1).coor;
            Time uTime1 = new Time(userLocations.get(userItem).time);
            Time uTime2 = new Time(userLocations.get(userItem+1).time);


            double x1 = userTemp1.latitude;
            double x2 = userTemp2.latitude;
            double y1 = userTemp1.longitude;
            double y2 = userTemp2.longitude;

            for (int j = 0; j < patientLocations.size()-1; j++) {
                LatLng patientTemp1 = patientLocations.get(j).coor;
                LatLng patientTemp2 = patientLocations.get(j+1).coor;
                Time pTime1 = new Time(patientLocations.get(j).time);
                Time pTime2 = new Time(patientLocations.get(j+1).time);


                // Fastest method, based on Franklin Antonio's "Faster Line Segment Intersection"
                // topic "in Graphics Gems III" book (http://www.graphicsgems.org/)
                // copied from http://www.java-gaming.org/index.php?topic=22590.0
                double x3 = patientTemp1.latitude;
                double x4 = patientTemp2.latitude;
                double y3 = patientTemp1.longitude;
                double y4 = patientTemp2.longitude;

                double ax = x2-x1;
                double ay = y2-y1;
                double bx = x3-x4;
                double by = y3-y4;
                double cx = x1-x3;
                double cy = y1-y3;

                double alphaNumerator = by*cx - bx*cy;
                double commonDenominator = ay*bx - ax*by;

                if (commonDenominator > 0){
                    if (alphaNumerator < 0 || alphaNumerator > commonDenominator){
                        continue;
                    }
                }else if (commonDenominator < 0){
                    if (alphaNumerator > 0 || alphaNumerator < commonDenominator){
                        continue;
                    }
                }
                double betaNumerator = ax*cy - ay*cx;
                if (commonDenominator > 0){
                    if (betaNumerator < 0 || betaNumerator > commonDenominator){
                        continue;
                    }
                }else if (commonDenominator < 0){
                    if (betaNumerator > 0 || betaNumerator < commonDenominator){
                        continue;
                    }
                }
                if (commonDenominator == 0){
                    // This code wasn't in Franklin Antonio's method. It was added by Keith Woodward.
                    // The lines are parallel.
                    // Check if they're collinear.
                    double y3LessY1 = y3-y1;
                    double collinearityTestForP3 = x1*(y2-y3) + x2*(y3LessY1) + x3*(y1-y2);   // see http://mathworld.wolfram.com/Collinear.html
                    // If p3 is collinear with p1 and p2 then p4 will also be collinear, since p1-p2 is parallel with p3-p4
                    if (collinearityTestForP3 == 0){
                        // The lines are collinear. Now check if they overlap.
                        if (x1 >= x3 && x1 <= x4 || x1 <= x3 && x1 >= x4 ||
                                x2 >= x3 && x2 <= x4 || x2 <= x3 && x2 >= x4 ||
                                x3 >= x1 && x3 <= x2 || x3 <= x1 && x3 >= x2){
                            if (y1 >= y3 && y1 <= y4 || y1 <= y3 && y1 >= y4 ||
                                    y2 >= y3 && y2 <= y4 || y2 <= y3 && y2 >= y4 ||
                                    y3 >= y1 && y3 <= y2 || y3 <= y1 && y3 >= y2){

                                if (timeIntersect(uTime1, uTime2, pTime1, pTime2)){
                                    LatLng intersection = getLineLineIntersection(x1, y1, x2, y2, x3, y3, x4, y4);
                                    if (intersection!= null){
                                        intersectionPoints.add(intersection);
                                    }
                                }

                            }
                        }
                    }
                    continue;
                }

                if (timeIntersect(uTime1, uTime2, pTime1, pTime2)){
                    LatLng intersection = getLineLineIntersection(x1, y1, x2, y2, x3, y3, x4, y4);
                    if (intersection!= null){
                        intersectionPoints.add(intersection);
                    }
                }

                log.i("RM", "size of intersection points " + String.valueOf(intersectionPoints.size()));

            }

        }

    public boolean timeIntersect (Time u1, Time u2, Time p1, Time p2){
        //if there's an overlap of time
        Calendar p1C = p1.getTimeC();
        Calendar p2C = p2.getTimeC();
        Calendar u1C = u1.getTimeC();
        Calendar u2C = u2.getTimeC();
        if (u1.timeBetween(p1C, p2C) ||
                u2.timeBetween(p1C, p2C) ||
                p1.timeBetween(u1C, u2C)||
                p2. timeBetween(u1C, u2C) ||
                u1.closeOnTime(p1C) ||
                u1.closeOnTime(p2C) ||
                u2.closeOnTime(p1C) ||
                u2.closeOnTime(p2C)) {

            return true;
        }
        else
            return false;

    }

    //finds the intersection point of two line segments
    // copied from http://www.java-gaming.org/index.php?topic=22590.0
    public LatLng getLineLineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double det1And2 = det(x1, y1, x2, y2);
            double det3And4 = det(x3, y3, x4, y4);
            double x1LessX2 = x1 - x2;
            double y1LessY2 = y1 - y2;
            double x3LessX4 = x3 - x4;
            double y3LessY4 = y3 - y4;
            double det1Less2And3Less4 = det(x1LessX2, y1LessY2, x3LessX4, y3LessY4);
            if (det1Less2And3Less4 == 0){
                // the denominator is zero so the lines are parallel and there's either no solution
                // (or multiple solutions if the lines overlap) so return null.
                return null;
            }
            double x = (det(det1And2, x1LessX2,det3And4, x3LessX4) / det1Less2And3Less4);
            double y = (det(det1And2, y1LessY2,det3And4, y3LessY4) /det1Less2And3Less4);
              return new LatLng(x, y);
    }

    //copied from http://www.java-gaming.org/index.php?topic=22590.0
    protected double det(double a, double b, double c, double d) {
        return a * d - b * c;
    }

    }

    public boolean isEmpty(){
        while (mThreadPool.getActiveCount()>0) {};

        return true;
    }

    public List<LocationStruct> getUserLocations(){

        return userLocations;
    }

    public List<LocationStruct> getPatientLocations(){

        return patientLocations;

    }

    public List<LatLng> getEffectedPoints(){

        return effectedPoints;

    }

    public List<LatLng> getIntersectionPoints (){

        return intersectionPoints;

    }

}
