package com.example.ugri.pathogion;


/**
 * FragmentActivity that controls all fragment activities and initialize thread management.
 */
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity {

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction; // same as fragmentManager.beginTransaction()

    //declare fragments
    UserLocation userLoc = new UserLocation();  //fragment without UI, records location updates
    Map map = new Map();                //show map and mapping
    ShowPath showPath = new ShowPath(); //fragment for user's tracks
    SideMenu sideMenu = new SideMenu();
    RunnableManagement mRunMgnt = new RunnableManagement();


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

        if (fragment == null) {

            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, 0)
                    .add(R.id.fragment_container, sideMenu, "side menu")
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

        //fetch user and patient location information of the given date
        mRunMgnt.setRunnableManagement(this, selectedDate);
        mRunMgnt.executeAll();

        while (!mRunMgnt.isEmpty()){};
        afterAllNeededArrays();
    }

    public void afterAllNeededArrays(){
        map.executeAll();
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
                .commit();
    }

    public List<LocationStruct> getUserLocations(){

        return mRunMgnt.getUserLocations();
    }

    public List<LocationStruct> getPatientLocations(){

        return mRunMgnt.getPatientLocations();

    }

    public List<LatLng> getEffectedPoints(){

        return mRunMgnt.getEffectedPoints();

    }

    public List<LatLng> getIntersectionPoints (){

        return mRunMgnt.getIntersectionPoints();

    }


}

