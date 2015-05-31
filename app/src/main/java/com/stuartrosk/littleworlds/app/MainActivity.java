package com.stuartrosk.littleworlds.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;

public class MainActivity extends Activity {

    private HomeFragment fragmentHome;
    private EditFragment fragmentEdit;
    private SharedPreferences preferences;

    public boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WorldService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startWorldService() {
        startService(new Intent(getApplicationContext(), WorldService.class));
    }

    public void stopWorldService() {
        stopService(new Intent(getApplicationContext(), WorldService.class));
    }

    public void showEditScreen() {
        getFragmentManager().beginTransaction()
            .replace(R.id.mainFrame, fragmentEdit)
            .addToBackStack(null)
        .commit();
    }

    public void finishedEditing() {
        getFragmentManager().beginTransaction()
            .replace(R.id.mainFrame, fragmentHome)
            .addToBackStack(null)
        .commit();
        ///reset world service
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentHome = new HomeFragment();
        fragmentEdit = new EditFragment();

        getFragmentManager().beginTransaction()
            .add(R.id.mainFrame, fragmentHome)
        .commit();

        preferences = getPreferences(MODE_PRIVATE);

        //always tell it to disable edit mode initially
        preferences.edit().putBoolean(getString(R.string.editModePref), false).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
    @Override
     protected void onDestroy() {
        //if we are supposed to keep service, restart without the edit controls
        if(preferences.getBoolean("starting_edit",false)) {
            preferences.edit().putBoolean("starting_edit",false).commit();
        } else {
            if (!preferences.getBoolean("serviceEnabled", false)) {
                stopService(new Intent(getApplicationContext(), WorldService.class));
            } else {
                Intent i = new Intent(getApplicationContext(), WorldService.class);
                stopService(i);
                startService(i);
            }
        }

        super.onDestroy();
    }
    @Override
    protected void onPause() {
        //if we are supposed to keep service, restart without the edit controls
        if(preferences.getBoolean("starting_edit",false)) {
            preferences.edit().putBoolean("starting_edit",false).commit();
        } else {
            if (!preferences.getBoolean("serviceEnabled", false)) {
                stopService(new Intent(getApplicationContext(), WorldService.class));
            } else {
                Intent i = new Intent(getApplicationContext(), WorldService.class);
                stopService(i);
                startService(i);
            }
        }

        super.onPause();
    }
    @Override
    protected void onResume() {
        //always just restart service with edit controls when resuming this screen
        Intent i = new Intent(getApplicationContext(), WorldService.class);
        stopService(i);
        i.putExtra("hideEditMode","true");
        startService(i);

        super.onResume();
    }
     */
}
