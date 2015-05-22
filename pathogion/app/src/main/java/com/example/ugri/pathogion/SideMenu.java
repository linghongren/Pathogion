package com.example.ugri.pathogion;

/**
 * side menu with options to mapping, delete database and show path date
 */

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.widget.TextView;



public class SideMenu extends ListFragment {

    String[]options = {"Show Path Date", "Delete Database","Clear Map","Return"};

    Log log;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        log.i("sideMenu", "oncreate");

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
                showPathDate();
                break;
            case 1:
//                clearDb();
                break;
        }

    }


    //call main activity's showPath function for "show path date"
    public void showPathDate(){
        ((MainActivity)getActivity()).showPath();
    }


/*    //a button
    public void clearDb(){
        log.i(" clearDb", "clear");
        getActivity().deleteDatabase("userLocationsData");
        Database db = new Database(getActivity().getApplicationContext());
        db.insertSampleData();
    }*/
}
