package com.stuartrosk.littleworlds.app;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private ImageButton mainEditButton;
    private Switch toggleSwitch;
    private SharedPreferences preferences;
    private MainActivity ma;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mainEditButton = (ImageButton)v.findViewById(R.id.mainEditButton);
        toggleSwitch = (Switch)v.findViewById(R.id.serviceSwitch);

        ma = (MainActivity)getActivity();
        preferences = ma.getPreferences(ma.MODE_PRIVATE);

        boolean firstTimer = preferences.getBoolean("firstTimer",true);
        if(firstTimer) {
            toggleSwitch.setChecked(true);
            preferences.edit().putBoolean("firstTimer",false).commit();
            ma.startWorldService();
        } else {
            boolean currServiceValue = preferences.getBoolean("serviceEnabled", false);
            preferences.edit().putBoolean("serviceEnabled", currServiceValue).commit();
            toggleSwitch.setChecked(currServiceValue);
            if(currServiceValue)
                ma.startWorldService();
            else
                ma.stopWorldService();
        }

        mainEditButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ma.showEditScreen();
                return true;
            }
        });

        toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (!ma.isServiceRunning() && isChecked)
                    ma.startWorldService();
                else
                    ma.stopWorldService();
                preferences.edit().putBoolean("serviceEnabled", isChecked).commit();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        boolean currServiceValue = preferences.getBoolean("serviceEnabled", false);
        if(currServiceValue) {
            ma.startWorldService();
            toggleSwitch.setChecked(true);
        } else {
            ma.stopWorldService();
            toggleSwitch.setChecked(false);
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
