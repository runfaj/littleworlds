package com.stuartrosk.littleworlds.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

public class ImageEditActivity extends Activity {

    private SharedPreferences preferences;

    private void handleRadios(RadioButton selected) {
        ViewGroup v = (ViewGroup) findViewById(R.id.edit_radiogroup);
        for(int i=0;i<v.getChildCount();i++) {
            if(v.getChildAt(i) instanceof RadioButton) {
                if(!v.getChildAt(i).equals(selected))
                    ((RadioButton) v.getChildAt(i)).setChecked(false);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_select_view);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //add various handlers
        ViewGroup v = (ViewGroup) findViewById(R.id.edit_radiogroup);
        for(int i=0;i<v.getChildCount();i++) {
            if(v.getChildAt(i) instanceof RadioButton) {
                v.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleRadios((RadioButton)findViewById(v.getId()));
                    }
                });
            }
        }
        Button cancelBtn = (Button)findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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

        super.onPause();
    }
}