package com.example.ugri.pathogion;


/**
 * FragmentActivity that controls all fragment activities and initialize thread management.
 */
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.content.Context;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainActivity extends FragmentActivity {

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction; // same as fragmentManager.beginTransaction()

    //declare fragments
    UserLocation userLoc = new UserLocation();  //fragment without UI, records location updates
    Map map = new Map();                //show map and mapping
    ShowPath showPath = new ShowPath(); //fragment for user's tracks
    SideMenu sideMenu = new SideMenu();
    RunnableManagement mRunMgnt = new RunnableManagement();

    List<LocationStruct> userLocations = new ArrayList();   //array with user's locations
    List<LocationStruct> patientLocations = new ArrayList<>(); //array with matched patient's locations.
    List<LatLng> effectedPoints = new ArrayList<>();
    List<LatLng> intersectionPoints = new ArrayList<>();

    String selectedDate = "";   //selected Date to compare

    Log log; // for logcat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            //add fragment to the activity one by one. The order is important
            fragmentManager.beginTransaction()
                    .add(userLoc, "Location Update")
                    .commit();

            fragmentManager.beginTransaction()
                    .add(map, "map")
                    .commit();
        }
    }

    //show ShowPath fragment
    public void showSideMenu(View view){
        log.i("main","show path");

        Fragment fragment = fragmentManager.findFragmentByTag("side menu");
        Fragment fragment2 = fragmentManager.findFragmentByTag("show path");

        //in case of clicking the button when show path fragment is added.
        if (fragment == null && fragment2!=null) {
            log.i("main", "side menu new");
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, 0)
                    .add(R.id.fragment_container, sideMenu, "side menu")
                    .commit();
        }
        else{
            log.i("main", "side menu replace");
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, sideMenu, "side menu")
                    .commit();
        }
    }

    //show ShowPath fragment
    public void showPath(){
        log.i("main","show path");

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0)
                .replace(R.id.fragment_container, showPath, "show path")
                .commit();
    }

    //called from ShowPath
    //find four needed arrays of information
    public void afterADateIsSelected(String sd){
        //get the sd from ShowPath and save to selectedDate;
        setSelectedDate (sd);

        //remove the showpath fragment
        removeShowPath();

        //all fetch user and patient location information of the given date
        mRunMgnt.setRunnableManagement(this, selectedDate);
        mRunMgnt.executeAll();

        while (!mRunMgnt.isEmpty()){};
    }

    public void afterAllNeededArrays(){
        log.i("main", "after all needed array");
        Fragment fragment = fragmentManager.findFragmentByTag("map");


        //map.executeAll();
    }

    //set selectedDate
    public void setSelectedDate(String sd) {
        selectedDate = sd;
        log.i("main", "selected " + selectedDate);

        removeShowPath();
    }

    public void removeShowPath(){
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_out_left, 0)
                .detach(showPath)
                .detach(sideMenu)
                .commit();
    }


    Handler mHandler;
    private class RunnableManagement {
//        Log log;

  //      Handler mHandler;


        //for finding intersection lines
        private class PointStruct {
            double coefficient;
            double yIntercept;
        }

        List<PointStruct> eqUser1 = new ArrayList<>();
        List<PointStruct> eqPatient2 = new ArrayList<>();

        int EFFECTED_RADIUS = 100;

        String date;
        Database db;
        int uSize = 0;
        int pSize = 0;

        private int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        private final BlockingDeque<Runnable> mRunnableQueue = new LinkedBlockingDeque<Runnable>();

        // Sets the amount of time an idle thread waits before terminating
        private static final int KEEP_ALIVE_TIME = 1;
        // Sets the Time Unit to seconds
        private final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        // Creates a thread pool manager
        ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mRunnableQueue);

        RunnableManagement() {
            log.i("rmgnt", "constructor");
            //a handler that attaches to the UI thread

            mHandler = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage (Message inputMessage) {
                    super.handleMessage(inputMessage);
                    if (inputMessage.what==0){
                        log.i ("main", "handle get control");
                        afterAllNeededArrays();
                    }
                }
            };

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

        boolean executeAll() {
            log.i("rmgnt", "executeall 1");
            mThreadPool.execute(fetchUserLocation);
            mThreadPool.execute(fetchPatientLocation);

            while (mThreadPool.getActiveCount() > 0) {
            }
            ;

/*
            log.i("rmgnt", "executeall 2");
            findEffectedPoints();
            findEquations();

            while (mThreadPool.getActiveCount() > 0) {
            }
            ;

            log.i("rmgnt", "executeall 3");
            findEffectedIntersectionLine();

            while (mThreadPool.getActiveCount() > 0) {
            }
            ;

*/
            log.i("rmgnt", "userLoc " + String.valueOf(userLocations.size()));
            log.i("rmgnt", "patientLoc " + String.valueOf(patientLocations.size()));

            Message completeMessage = Message.obtain();
            completeMessage.what = 0;
            completeMessage.setTarget(mHandler);
            completeMessage.sendToTarget();

            return true;
        }


        //find effected points by comparing two arrays of points
        void findEffectedPoints() {
            uSize = userLocations.size();
            pSize = patientLocations.size();

            if (uSize > 0 && pSize > 0) {

                for (int i = 0; i < pSize; i++) {
                    CompareLocations cl = new CompareLocations(i);
                    mThreadPool.execute(cl);
                }
            }
        }

        void findEquations() {
            if (uSize > 0 && pSize > 0) {
                LineEquationUser eqUser = new LineEquationUser();
                mThreadPool.execute(eqUser);

                LineEquationPatient eqPatient = new LineEquationPatient();
                mThreadPool.execute(eqPatient);

            }
        }

        //find the intersection points between two paths
        //after all equations are found
        void findEffectedIntersectionLine() {
            for (int i = 0; i < eqUser1.size() - 1; i++) {
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

                for (int i = 0; i < userLocations.size(); i++) {
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


        //find effected intersection line equation
        private class LineEquationUser implements Runnable {

            @Override
            public void run() {
                log.i("RM", "find equations");
                int lsTemp = uSize;
                if (lsTemp > 0) {
                    for (int i = 1; i < uSize; i++) {
                        PointStruct pt = new PointStruct();
                        //(y2 - y1 )/ (x2 - x1)
                        pt.coefficient = (userLocations.get(i).coor.latitude - userLocations.get(i - 1).coor.latitude) /
                                (userLocations.get(i).coor.longitude - userLocations.get(i - 1).coor.longitude);

                        if (Double.isNaN(pt.coefficient))
                            pt.coefficient = (double) 0;

                        pt.yIntercept
                                = userLocations.get(i - 1).coor.longitude -
                                pt.coefficient * userLocations.get(i - 1).coor.latitude;

//                  log.i("map", "coef " + String.valueOf(pt.coefficient) + " " + String.valueOf(pt.yIntercept));
                        eqUser1.add(pt);
                    }
                }
            }
        }

        //find effected intersection line equation
        private class LineEquationPatient implements Runnable {
            @Override
            public void run() {
                log.i("RM", "find equations");
                int lsTemp = uSize;
                if (lsTemp > 0) {
                    for (int i = 1; i < uSize; i++) {
                        PointStruct pt = new PointStruct();
                        //(y2 - y1 )/ (x2 - x1)
                        pt.coefficient = (patientLocations.get(i).coor.latitude - patientLocations.get(i - 1).coor.latitude) /
                                (patientLocations.get(i).coor.longitude - patientLocations.get(i - 1).coor.longitude);

                        if (Double.isNaN(pt.coefficient))
                            pt.coefficient = (double) 0;

                        pt.yIntercept
                                = patientLocations.get(i - 1).coor.longitude -
                                pt.coefficient * patientLocations.get(i - 1).coor.latitude;

//                  log.i("map", "coef " + String.valueOf(pt.coefficient) + " " + String.valueOf(pt.yIntercept));
                        eqPatient2.add(pt);
                    }
                }
            }
        }


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
                //Time uTime1 = new Time(locU.get(i).time);
                //Time uTime2 = new Time(locU.get(i+1).time);


                double coef = eqUser1.get(userItem).coefficient;
                double intercept = eqUser1.get(userItem + 1).yIntercept;
                //log.i("map", "find line " + String.valueOf(coef) + " " + String.valueOf(intercept));
                for (int j = 0; j < eqPatient2.size(); j++) {
                    double x = (eqPatient2.get(j).yIntercept - intercept) /
                            (coef - eqPatient2.get(j).coefficient);

                    //log.i("map", "get double " +String.valueOf(x));
                    if (Double.isInfinite(x))
                        x = (double) 0;

                    //log.i("map", "longitudes "+ String.valueOf(userTemp1.latitude) + " " + String.valueOf(userTemp2.latitude));
                    //if the intersection point is between user's points
                    if ((x < userTemp1.latitude && x > userTemp2.latitude)
                            || (x < userTemp2.latitude && x > userTemp1.latitude)) {
                        double y = (coef * x) + intercept;
                        //log.i("map", "get double y " +String.valueOf(y));

                        //log.i("map", "longitudes "+ String.valueOf(userTemp1.longitude) + " " + String.valueOf(userTemp2.longitude));
                        if ((y < userTemp1.longitude && y > userTemp2.longitude)
                                || (y < userTemp2.longitude && y > userTemp1.longitude)) {
                            intersectionPoints.add(new LatLng(x, y));

                        }
                    }
                }
                log.i("RM", "size of intersection points " + String.valueOf(intersectionPoints.size()));

            }

        }

        public boolean isEmpty(){
            while(!mRunnableQueue.isEmpty()){};

            return true;
        }

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

