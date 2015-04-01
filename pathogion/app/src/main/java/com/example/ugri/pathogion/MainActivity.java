package com.example.ugri.pathogion;

import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;


public class MainActivity extends FragmentActivity {

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction;


    UserLocation userLoc = new UserLocation();
    SideMenu sideMenu = new SideMenu();
    Map map = new Map();

    Log log;
    static final String dateFormat = "yyyy-MM-dd HH:mm:ss";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //code map to the app


        fragmentTransaction = fragmentManager.beginTransaction();
        //fragment with no user interface
        fragmentTransaction.add(userLoc, "Location Update");

        if (findViewById(R.id.fragment_container) !=null){
            fragmentTransaction.add(R.id.fragment_container,sideMenu, "Side Bar" );
            fragmentTransaction.add(R.id.fragment_container,map, "map");
            log.i("main activity", "fragment_container");
            fragmentTransaction.hide(sideMenu);
            fragmentTransaction.show(map);

        }

        fragmentTransaction.commit();

    }

    //show sideMenu
    public void callSideMenu(View view){
        fragmentManager.beginTransaction()
                       .setCustomAnimations(R.anim.slide_in_right, 0)
                       .show(sideMenu)
                       .commit();
    }

    public void hideSideMenu(){
        //log.i ("main", "hide");
        fragmentManager.beginTransaction()
                .setCustomAnimations(0, R.anim.slide_out_left)
                .hide(sideMenu)
                .commit();
    }

}

