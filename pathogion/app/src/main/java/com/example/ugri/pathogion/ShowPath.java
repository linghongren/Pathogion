package com.example.ugri.pathogion;

/**
 * list out available dates in the database
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

import java.util.ArrayList;
import java.util.List;



public class ShowPath extends ListFragment {

    Log log;

    List<String> pathDate = new ArrayList<>();
    Database db;

    String selected="";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        db = new Database(getActivity());

        log.i("showPath", "oncreate");

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater,container,savedInstanceState);

        log.i("showPath", "created");
        View view = inflater.inflate(R.layout.side_menu, container,false);

        //set title
        TextView textView = (TextView)view.findViewById(R.id.title);
        textView.setText("Show Path Date");

        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        getAvailableDate();

        setListAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, pathDate));
        getListView().setSelector(R.drawable.show_path_select);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        log.i("showpath", String.valueOf(position));
            //when an date is selected, pass the date to fragmentActivity
        selected = pathDate.get(position);
        ((MainActivity)getActivity()).setSelectedDate(selected);
        showPTrack();

    }

    //get all dates in the database
    public void getAvailableDate (){
        log.i("showpath", "get date");
        pathDate = db.existingDates();
        log.i("showpath", "get date finish");
    }


    //pass the selected date to fragmentActivity
/*    //calling main activity's hidePathList function.
    public void goBack(){
        ((MainActivity)getActivity()).hidePathList(); //pass date to main activity
    }
*/
    //Call fragmentActivity's showPTrack()
    public void showPTrack(){
        ((MainActivity)getActivity()).showPTrack();
    }

}

