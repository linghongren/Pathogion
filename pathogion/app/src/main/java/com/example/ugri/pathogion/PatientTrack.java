package com.example.ugri.pathogion;

/**
 * Parse Geojson file that has Patient's track info
 */


import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class PatientTrack {

    Log log;
    File file;
    InputStream inputStream;
    List <LocationStruct> tracks = new ArrayList<>();       //patient's locations
    String date;


    PatientTrack(String dt){
        //get file link
        getFile();
        date = dt;

    }
    public void getFile() {
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
    public void patientLookUp ( ) {
        log.i("patientTrack getTime", date);
        if (! date.equals("") ){
            findPatientLocation();
        }
    }

    //read geojson file
    public void findPatientLocation () {

        try {
            inputStream = new FileInputStream(file);
            //read through the geojson file
            tracks.clear();
            readJsonStream();
            //filter again

        } catch (IOException e) {
            log.i("patientTrack", "on Create error");

        }
        //goBack();

    }

    public List <LocationStruct> getPatientLocations (){

        log.i ("ptrack", "finish reading " + String.valueOf(tracks.size()));
        return tracks;
    }



    //parsing the geojson file
    public void readJsonStream() throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            log.i("patientTrack", "readJsonStream");
            readMessagesArray(reader);
        }
        finally {
            reader.close();
        }
    }

    //get all latitude and longitude on wantedTime in reader
    //read all features
    public void readMessagesArray(JsonReader reader) throws IOException {

        LocationStruct temp;

        //parse the beginning of the file
        reader.beginObject();
        while (reader.hasNext()){
            String name = reader.nextName();

            if (name.equals("features")){
                reader.beginArray();
                //start parsing features
                while (reader.hasNext()){
                    readFeatures(reader); //get coordinate
                }
                reader.endArray();
            }
            else
                reader.skipValue();
        }

        reader.endObject();
    }

    //read features in reader
    //set isNeeded to true if this coordinate is on the wantTime
    public void readFeatures (JsonReader reader) throws IOException{
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

        Time time1 = new Time(date, 1);
        Time time2 = new Time(features.time);


        //compare if two dates are the same
        //if yes, set isNeeded to true;
        //if not, set isNeeded to false;
        if (time1.onSameDay(time2.getTimeC()))
            tracks.add(features);
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

}
