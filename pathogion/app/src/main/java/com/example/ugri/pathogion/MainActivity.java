package com.example.ugri.pathogion;


/**
 * FragmentActivity that controls communications between fragments
 */
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
    SideMenu sideMenu = new SideMenu(); //side menu with options
    Map map = new Map();                //show map and mapping
    ShowPath showPath = new ShowPath(); //fragment for user's tracks
    PatientTrack pTrack = new PatientTrack();   //fragment for patient's tracks

    List<LocationStruct> userLocations = new ArrayList();   //array with user's locations
    List<LocationStruct> patientLocations = new ArrayList<>(); //array with matched patient's locations.
    String selectedDate = "";   //selected Date to compare
    Bundle savedInstance;

    Database db;

    Log log; // for logcat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        savedInstance = savedInstanceState;
        if (savedInstanceState == null) {
            //add fragment to the activity one by one. The order is important
            fragmentManager.beginTransaction()
                    .add(userLoc, "Location Update")
                    .commit();

            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, map, "map")
                    .commit();
        }
    }


    //show SideMenu fragment
    public void callSideMenu(View view){
        log.i("main", "show side menu");

        if (fragmentManager.findFragmentByTag("side menu") == null) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, 0)
                    .add(R.id.fragment_container, sideMenu, "side menu")
                    .commit();
        }
        else {
            if (fragmentManager.findFragmentByTag("show path")!=null){
                fragmentManager.beginTransaction()
                        .remove(showPath)
                        .commit();
            }

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

    // Pass a selectedDate to PatientTrack fragment to find a matching
    // Then show PatientTrack fragment
    public void showPTrack (){
        log.i("main", "pTrack");
        fragmentManager.beginTransaction()
                .add(pTrack, "patient track")
                .commit();

        pTrack.patientLookUp(selectedDate);
    }

    //mapping user's and patient's locations on the map fragment
    public void showPathsOnMap(){
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_out_left, 0)
                .detach(showPath)
                .commit();

        Map fragmentM = (Map) fragmentManager.findFragmentByTag("map");
        log.i("main", "userloc size " + String.valueOf(userLocations.size())+
                "patientloc size "+ String.valueOf(patientLocations.size()));

        fragmentM.copyLocations(userLocations, patientLocations);
    }

    //set selectedDate
    public void setSelectedDate(String sd) {
        selectedDate = sd;
        log.i("main", "selected " + selectedDate);
        if (selectedDate != "") {
            new GetUserLocationOneDay().execute(selectedDate);
            log.i("main", "asynctask " + String.valueOf(userLocations.size()));
        }
    }

    //set patientLocations array
    public void setPatientLocations(List <LocationStruct> pLocs){
        patientLocations = pLocs;
        log.i("main size of pTracks", String.valueOf(patientLocations.size()));
        if (patientLocations.size()>1)
            log.i ("hidepatrack", "more than one record");
    }

    //set userLocations array
    public void setUserLocations(List<LocationStruct> ls){
        log.i("main", "getUserLocations");
        userLocations = ls;
    }

    //return userlocations array
    public List<LocationStruct> getUserLocations(){
        return userLocations;
    }

    private class GetUserLocationOneDay extends AsyncTask<String, Void, Long>{
        Database db = new Database(getApplicationContext());

        protected Long doInBackground(String... params){
            long result = 0;
            log.i("main", "async task "+ params[0]);
            db.getLatLngGivenDate(params[0]);
            return result;
        }

        protected void onPostExecute(Long result) {
            if (result == 0) {
                setUserLocations(db.passLatLngDate());
                log.i("main", "asynctask done " + String.valueOf(userLocations.size()));
            }
        }
    }

}

