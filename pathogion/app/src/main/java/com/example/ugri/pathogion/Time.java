package com.example.ugri.pathogion;

/**
 *
 * manage all helper functions like, time, distance
 */

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class Time {
    static final int MINIMAL_MINUTE = 5;
    private String timeS = "";
    private Date timeD = new Date();
    private Calendar timeC = Calendar.getInstance();

    String dateFormatG = "yyyy-MM-dd'T'HH:mm:ssZ"; // the format of time in the Geojson file
    SimpleDateFormat formatDateG = new SimpleDateFormat(dateFormatG, Locale.ENGLISH);

    String dateFormatL = "yyyy-MM-dd";   //the format of time on the list
    SimpleDateFormat formatDateL = new SimpleDateFormat(dateFormatL);

    Time (){
    }
    //constructor for database time
    Time (long t){
        timeD.setTime(t);
        timeC.setTimeInMillis(t);
        timeS = "";
    }

    //constructor for String
    //i == 0 for Geojson file, i == 1 for user's location
    Time (String t, int i){
        timeS = t;
        try {
            if ( 0 ==i )
                timeD = formatDateG.parse(t);   //Geojson file
            else
                timeD = formatDateL.parse(t);  // time on the list
        }catch (ParseException e){
            e.printStackTrace();
        }
        timeC.setTime(timeD);
    }

    Time (Date t){
        timeD = t;
        timeC.setTime(timeD);
    }

    void setTime(long t){
        timeD.setTime(t);
        timeC.setTimeInMillis(t);
        timeS = formatDateL.format (timeD);
    }

    String getTimeS (){
        return timeS;
    }

    Date getTimeD (){
        return timeD;
    }

    Calendar getTimeC (){
        return timeC;
    }

    boolean onSameDay (Calendar cal){
        return (timeC.get(Calendar.YEAR))==(cal.get(Calendar.YEAR)) &&
                (timeC.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR));
    }

    boolean closeOnTime (Calendar cal){
        boolean isFiveMin = false;
        if (Math.abs(timeC.get(Calendar.MINUTE) - cal.get(Calendar.MINUTE)) < MINIMAL_MINUTE)
            isFiveMin = true;

        return ((timeC.get(Calendar.HOUR_OF_DAY))==(cal.get(Calendar.HOUR_OF_DAY)) && isFiveMin);
    }

    boolean timeAfter (Calendar cal){
        return (timeC.after(cal) && (!closeOnTime(cal)));
    }

    boolean timeBetween (Calendar cal1, Calendar cal2){
        return ((timeC.after(cal1) && timeC.before(cal2))
                || timeC.after(cal2) && timeC.before(cal1));
    }

    boolean dayAfter (Calendar cal){
        boolean isAfter = false;
        if (timeC.get(Calendar.YEAR)>cal.get(Calendar.YEAR))
            isAfter = true;
        else {
            if (timeC.get(Calendar.DAY_OF_YEAR) >cal.get(Calendar.DAY_OF_YEAR))
                isAfter = true;
        }
        return isAfter;
    }

}

class LocationStruct{
    Date time;
    LatLng coor;
}

class Distance{
    Location loc = new Location ("distance");

    Distance (){
    }

    Distance (LatLng coor){
        loc.setLatitude(coor.latitude);
        loc.setLongitude(coor.longitude);
    }

    void setDistance (Distance d){
        loc.setLatitude(d.getLoc().getLatitude());
        loc.setLongitude(d.getLoc().getLongitude());
    }

    void setLoc(LatLng coor){
        loc.setLatitude(coor.latitude);
        loc.setLongitude(coor.longitude);
    }

    LatLng getLatLng(){
        LatLng llg = new LatLng(loc.getLatitude(),loc.getLongitude());

        return llg;
    }

    Location getLoc (){
        return loc;
    }

    float findDistance (Distance d){
        return loc.distanceTo(d.getLoc());
    }
}