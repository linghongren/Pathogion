package com.example.ugri.pathogion;

import android.app.DialogFragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by UGRI on 4/1/15.
 */
public class UserPath extends ListFragment {

    List<String>pathDate = new ArrayList<>();

    Database db = new Database(getActivity());

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        showPathDate();
        setListAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, pathDate));
    }


    public void showPathDate (){
        pathDate = db.existingDates();

    }
}
