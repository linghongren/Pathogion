package com.example.ugri.pathogion;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by UGRI on 5/21/15.
 */
public class PatientTrackAsync extends AsyncTask<String, Void, Long> {
    Log log;

    protected Long doInBackground(String... params){
        long result = 0;
        log.i("main", "async task "+ params[0]);
        return result;
    }

    protected void onPostExecute(Long result) {

}
