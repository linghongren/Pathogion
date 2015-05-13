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

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PatientTrack extends ListFragment {

    static final int MINIMAL_DISTANCE = 5;

    Log log;
    File file;
    InputStream inputStream;
    List <LocationStruct> userLocations = new ArrayList();  //user's locations
    List <LocationStruct> tracks = new ArrayList<>();       //patient's locations
    List <String> listItem = new ArrayList<>();     //List item on the fragment

    boolean isNeeded = false;

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
 //               if (!compareUserAndPatientLocation()){
 //                   tracks.clear();
 //               }

            } catch (IOException e) {
                log.i("patientTrack", "on Create error");

            }
        }
    }

    //parsing the geojson file
    public List <LocationStruct> readJsonStream(InputStream in, String wantedTime) throws IOException {
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
    public List <LocationStruct> readMessagesArray(JsonReader reader, String wantedTime) throws IOException {
        List <LocationStruct> coor = new ArrayList();

        LocationStruct temp;

        //parse the beginning of the file
        reader.beginObject();
        while (reader.hasNext()){
            String name = reader.nextName();

            if (name.equals("features")){
                reader.beginArray();
                //start parsing features
                while (reader.hasNext()){
                    temp = readFeatures(reader, wantedTime); //get coordinate
//                    if (isNeeded)   //a variable that shows whether a coordinate is on the given date
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
    public LocationStruct readFeatures (JsonReader reader, String wantTime) throws IOException{
        LocationStruct features = new LocationStruct();

        reader.beginObject();
        while (reader.hasNext()){
            String name = reader.nextName();
            if (name.equals("properties")){
                Time temp =new Time (readTimestamp(reader), 0);  //get time
                features.time =temp.getTimeD();
            }
            else if (name.equals("geometry"))
                features.coor = readCoordinates(reader); //get coordinates
            else
                reader.skipValue();
        }

        reader.endObject();

        Time time1 = new Time(wantTime, 1);
        Time time2 = new Time(features.time);


        //compare if two dates are the same
        //if yes, set isNeeded to true;
        //if not, set isNeeded to false;
        if (time1.onSameDay(time2.getTimeC()))
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
                longi = reader.nextDouble();
                lat= reader.nextDouble();
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

        boolean match = false;

        for (int i = 0; i < userLocations.size(); i ++){

            Distance userD = new Distance(userLocations.get(i).coor);
            Time timeUser = new Time(userLocations.get(i).time);

            for (int j =0; j < tracks.size(); j++){

                Distance userP = new Distance (tracks.get(j).coor);
                Time timepatient = new Time(userLocations.get(j).time);

                //locations are close and time are close
                if ((userD.findDistance(userP) < MINIMAL_DISTANCE )
                        && (timeUser.closeOnTime(timepatient.getTimeC()))){
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
