package com.example.ugri.pathogion;


/**
 * FragmentActivity that controls communications between fragments
 */
import android.app.FragmentManager;
import android.app.FragmentTransaction;

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
    SideMenu sideMenu = new SideMenu(); //side menu with options
    Map map = new Map();                //show map and mapping
    ShowPath showPath = new ShowPath(); //fragment for user's tracks
    PatientTrack pTrack = new PatientTrack();   //fragment for patient's tracks

    List<LatLng> userLocations = new ArrayList();   //array with user's locations
    List<LatLng> patientLocations = new ArrayList<>(); //array with matched patient's locations.
    String selectedDate = "";   //selected Date to compare

    Log log; // for logcat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //add fragment to the activity one by one. The order is important
        fragmentManager.beginTransaction()
                .add (userLoc, "Location Update")
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, map, "map")
                .show(map)
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, sideMenu, "side Menu")
                .hide(sideMenu)
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, showPath, "show user path")
                .hide(showPath)
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, pTrack, "patient track")
                .hide(pTrack)
                .commit();

    }


    //show SideMenu fragment
    public void callSideMenu(View view){
        log.i("main", "show side menu");
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0)
                .show(sideMenu)
                .commit();
    }

    //hide SideMenu fragment
    public void hideSideMenu(){
        log.i ("main", "hide side menu");
        fragmentManager.beginTransaction()
                .setCustomAnimations(0, R.anim.slide_out_left)
                .hide(sideMenu)
                .commit();
    }

    //show ShowPath fragment
    public void showPath(){
        log.i("main","show path");
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0)
                .show(showPath)
                .commit();
    }

    //hide ShowPath fragment
    public void hidePathList(){
        log.i ("main", "hide");

        fragmentManager.beginTransaction()
                .setCustomAnimations(0, R.anim.slide_out_left)
                .hide(showPath)
                .commit();
    }

    //set selectedDate
    public void setSelectedDate(String sd){
        selectedDate = sd;
    }

    // Pass a selectedDate to PatientTrack fragment to find a matching
    // Then show PatientTrack fragment
    public void showPTrack (){
        log.i("main", "pTrack");
        PatientTrack fragment = (PatientTrack) fragmentManager.findFragmentByTag("patient track");
        fragment.patientLookUp(selectedDate);
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right,0)
                .show(pTrack)
                .commit();
    }

    // Then hide PatientTrack fragment
    public void hidePTrack(){
        log.i("main", "hide pTrack");

        fragmentManager.beginTransaction()
                .setCustomAnimations(0, R.anim.slide_out_left)
                .hide(pTrack)
                .commit();
    }

    //set patientLocations array
    public void setPatientLocations(List <LatLng> pLocs){
        patientLocations = pLocs;
        log.i("main size of pTracks", String.valueOf(patientLocations.size()));
        if (patientLocations.size()>1)
            log.i ("hidepatrack", "more than one record");
    }

    //set userLocations array
    public void setUserLocations(List<LatLng> userLocs){
        log.i("main", "getUserLocations");
        userLocations = userLocs;
    }

    //return userlocations array
    public List<LatLng> getUserLocations(){
        return userLocations;
    }

    //mapping user's and patient's locations on the map fragment
    public void showPathsOnMap(){
        hideSideMenu();
        Map fragment = (Map) fragmentManager.findFragmentByTag("map");
        if (userLocations.size()!=0 || patientLocations.size()!=0)
            fragment.mapUserLocation(userLocations, patientLocations);

    }

    @Override
    public void onPause (){
        super.onPause();
        fragmentManager.beginTransaction()
                .remove(pTrack)
                .remove(showPath)
                .remove(sideMenu)
                .remove(map)
                .commit();
    }


}

