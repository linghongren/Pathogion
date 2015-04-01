package com.example.ugri.pathogion;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by UGRI on 3/25/15.
 */
public class SideMenu extends ListFragment {

    String[]options = {"Show Path Date", "Infected Patient","Delete Database", "Return"};

    Log log;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.side_menu, container,false);

        //set title
        TextView textView = (TextView)view.findViewById(R.id.title);
        textView.setText("Pathogion");

        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        setListAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, options));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        log.i("sideMenu", String.valueOf(position));
        switch (position){
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                goBack();
                break;
        }

    }

    @Override
    public void onPause (){
        super.onPause();
    }

    //calling main activity's hideSideMenu function.
    public void goBack(){
        ((MainActivity)getActivity()).hideSideMenu();
    }

    //a button
    public void clearDb(View view){
        log.i(" clearDb", "clear");
        getActivity().deleteDatabase("userLocationsData");
    }
}
