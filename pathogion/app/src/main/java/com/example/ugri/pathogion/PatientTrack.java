package com.example.ugri.pathogion;

/**
 * Parse Geojson file that has Patient's track info
 */

import android.app.ListFragment;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class PatientTrack extends ListFragment {

    Log log;
    File file;
    InputStream inputStream;
    List <LatLng> userLocations = new ArrayList();  //user's locations
    List <LatLng> tracks = new ArrayList<>();       //patient's locations
    List <String> listItem = new ArrayList<>();     //List item on the fragment

    boolean isNeeded = false;

    String dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"; // the format of time in the Geojson file
    SimpleDateFormat formatDate = new SimpleDateFormat(dateFormat, Locale.ENGLISH);

    String dateFormatDatabase = "yyyy-MM-dd";   //the format of time in the user's locations
    SimpleDateFormat formatDateDatabase = new SimpleDateFormat(dateFormatDatabase);


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //get path to Download file in the device
        String uri = Environment.getExternalStorageDirectory().toString();
        uri = uri +"/Download/patient.geojson";

        log.i("patientTrack", uri);
        file = new File(uri);
        if (file != null)
            log.i("patientTrack", "onCreate file good");
        //else

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.show_path, container,false);

        //set title
        TextView textView = (TextView)view.findViewById(R.id.title);
        textView.setText("Patient Track");

        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        log.i("PatientTrack", "onactivity");

        //should be changed when there are more than one patient with names
        listItem.add("One patient");

        listItem.add("Back");
        setListAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listItem));

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        log.i("showpath", String.valueOf(position));

        if (position == listItem.size()-1){
            goBack();
        }
        //add more when there are more than one patients
    }

    //look up matching patient on the selectedDate in the file
    public void patientLookUp ( String selectedDate ) {
        String wantedTime = selectedDate;
        log.i("patientTrack getTime", wantedTime);
        if (! wantedTime.equals("") ){
            try {
                inputStream = new FileInputStream(file);
                log.i("patientTrack", "onCreate 2");
                //read through the geojson file
                tracks = readJsonStream(inputStream, wantedTime);
                //filter again
                if (!compareUserAndPatientLocation()){
                    tracks.clear();
                }

            } catch (IOException e) {
                log.i("patientTrack", "on Create error");

            }
        }
    }

    //parsing the geojson file
    public List <LatLng> readJsonStream(InputStream in, String wantedTime) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            log.i("patientTrack", "readJsonStream");
            return readMessagesArray(reader, wantedTime);
        }
            finally {
                reader.close();
        }
    }

    //get all latitude and longitude on wantedTime in reader
    //read all features
    public List <LatLng> readMessagesArray(JsonReader reader, String wantedTime) throws IOException {
        List <LatLng> coor = new ArrayList();

        LatLng temp;

        //parse the beginning of the file
        reader.beginObject();
        while (reader.hasNext()){
            String name = reader.nextName();

            if (name.equals("features")){
                reader.beginArray();
                //start parsing features
                while (reader.hasNext()){
                    temp = readFeatures(reader, wantedTime); //get coordinate
                    if (isNeeded)   //a variable that shows whether a coordinate is on the given date
                        coor.add(temp);
                }
                reader.endArray();
            }
            else
                reader.skipValue();
        }

        reader.endObject();

        return coor;
    }

    //read features in reader
    //set isNeeded to true if this coordinate is on the wantTime
    public LatLng readFeatures (JsonReader reader, String wantTime) throws IOException{
        LatLng features = new LatLng(0, 0);
        String timestamp = "";
        Date wantTimeD = new Date(); // the time of the location we look for
        Date timeD = new Date();  // the time that patient file has

        reader.beginObject();
        while (reader.hasNext()){
            String name = reader.nextName();
            if (name.equals("properties"))
                timestamp = readTimestamp(reader);  //get time
            else if (name.equals("geometry"))
                features = readCoordinates(reader); //get coordinates
            else
                reader.skipValue();
        }

        reader.endObject();

        //change the format of the time
        try {
            timeD = formatDate.parse(timestamp);
            wantTimeD = formatDateDatabase.parse (wantTime);
        }catch (ParseException e){
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(wantTimeD);
        cal2.setTime(timeD);

        //compare if two dates are the same
        //if yes, set isNeeded to true;
        //if not, set isNeeded to false;
        if ((cal1.get(Calendar.YEAR))==(cal2.get(Calendar.YEAR)) &&
                (cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)))
            isNeeded = true;

        else
            isNeeded = false;


        return features;
    }

    //read a time in reader
    public String readTimestamp(JsonReader reader) throws IOException{
        String time = "";

        reader.beginObject();
        while (reader.hasNext()){
            String name = reader.nextName();
            if (name.equals("timestamp"))
                time = reader.nextString();
            else
                reader.skipValue();
        }

        reader.endObject();

        return time;
    }

    //read a pair of coordinates
    public LatLng readCoordinates(JsonReader reader) throws IOException{

        double lat = 0;
        double longi = 0;
        reader.beginObject();

        while (reader.hasNext()){
            String name = reader.nextName();
            if (name.equals("coordinates")){
                reader.beginArray();
                lat = reader.nextDouble();
                longi= reader.nextDouble();
                reader.endArray();
            }
            else
                reader.skipValue();
        }
        reader.endObject();

//        log.i("patientTrack read coordinate", String.valueOf(lat)+ " " + String.valueOf(longi));

        return new LatLng(lat, longi);

    }

    //compare user's locations with Patient's Location.
    // If any of the two locations is less than 10 miles away, return true; otherwise, false
    public boolean compareUserAndPatientLocation(){
        setUserLocations();     //get user's locations
        Location userLoc = new Location("Me");
        Location pLoc = new Location("Me");
        boolean match = false;
        for (int i = 0; i < userLocations.size(); i ++){
            userLoc.setLatitude(userLocations.get(i).latitude);
            userLoc.setLongitude(userLocations.get(i).longitude);
            for (int j =0; j < tracks.size(); j++){
                pLoc.setLatitude(tracks.get(j).latitude);
                pLoc.setLongitude(tracks.get(j).longitude);

                if (userLoc.distanceTo(pLoc) < 10){
                    match = true;
                    break;
                }

            }

            if (match)
                break;
        }

        return match;
    }

    //get userLocations from fragmentActivity
    public void setUserLocations(){
        userLocations = ((MainActivity)getActivity()).getUserLocations();
    }

    //pass the patient's locations to fragmentActivity
    //Call FragmentActivity's hidePTrack();
    public void goBack(){
        ((MainActivity)getActivity()).setPatientLocations(tracks);
        ((MainActivity)getActivity()).hidePTrack();
    }

}
