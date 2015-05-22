package com.example.ugri.pathogion;

/**
 * Parse Geojson file that has Patient's track info
 */

import android.app.ListFragment;
import android.os.AsyncTask;
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
import java.util.ArrayList;
import java.util.List;


public class PatientTrack extends ListFragment {

    Log log;
    File file;
    InputStream inputStream;
    List <LocationStruct> userLocations = new ArrayList();  //user's locations
    List <LocationStruct> tracks = new ArrayList<>();       //patient's locations

    getPatientLocation asyncTask;
    boolean isNeeded = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //get path to Download file in the device
        String uri = Environment.getExternalStorageDirectory().toString();
        uri = uri +"/Download/patient.geojson";
        log.i("patientTrack", "onCreate");
        log.i("patientTrack", uri);
        file = new File(uri);
        if (file != null)
            log.i("patientTrack", "onCreate file good");
        //else

    }

    //look up matching patient on the selectedDate in the file
    public void patientLookUp ( String selectedDate ) {
        String wantedTime = selectedDate;
        log.i("patientTrack getTime", wantedTime);
        if (! wantedTime.equals("") ){
            asyncTask = new getPatientLocation();
            asyncTask.execute(wantedTime);
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

    //get userLocations from fragmentActivity
    public void setUserLocations(){
        userLocations = ((MainActivity)getActivity()).getUserLocations();
    }

/*    //pass the patient's locations to fragmentActivity
    //Call FragmentActivity's hidePTrack();
    public void goBack(){
        ((MainActivity)getActivity()).hidePTrack();

    }
*/
    //asynctask to read geojson file
    public class getPatientLocation extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params){

            try {
                inputStream = new FileInputStream(file);
                //read through the geojson file
                tracks.clear();
                tracks = readJsonStream(inputStream, params[0]);
                //filter again

            } catch (IOException e) {
                log.i("patientTrack", "on Create error");

            }
            return null;
        }
        protected void onPostExecute (Void result){
            ((MainActivity)getActivity()).setPatientLocations(tracks);
            log.i ("ptrack", "finish reading " + String.valueOf(tracks.size()));
            //goBack();
        }

    }

/************************************************************
 *
 * if more than one patient exists in the data
@Override
public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    super.onCreateView(inflater,container,savedInstanceState);
    View view = inflater.inflate(R.layout.side_menu, container,false);

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

        setListAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listItem));

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        log.i("showpath", String.valueOf(position));

        //add more when there are more than one patients
    }

**************************************************************/
}
