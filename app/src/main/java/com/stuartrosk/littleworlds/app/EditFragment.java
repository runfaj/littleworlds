package com.stuartrosk.littleworlds.app;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditFragment extends Fragment {

    private SharedPreferences preferences;
    private MainActivity ma;
    private Button editDoneBtn;

    public EditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit, container, false);

        ma = (MainActivity)getActivity();
        preferences = ma.getPreferences(ma.MODE_PRIVATE);

        //init fragment
        getFragmentManager().beginTransaction()
            .replace(R.id.myPrefFragmentCont, new PreferenceListFragment())
        .commit();

        editDoneBtn = (Button)v.findViewById(R.id.editDoneBtn);

        editDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ma.finishedEditing();
            }
        });

        //set edit button handlers
        ViewGroup vg = (ViewGroup)v.findViewById(R.id.editScreen);
        for(int i=0;i<vg.getChildCount();i++) {
            if(vg.getChildAt(i) instanceof ImageButton) {
                vg.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int btnID = v.getId();
                        //preferences.edit().putString(getString(R.string.editModePos),"PUT EDIT POS HERE");
                        //show dialog with pos
                    }
                });
            }
        }

        //always just restart service without edit controls oncreate
        preferences.edit().putBoolean(getString(R.string.editModePref),false).commit();

        return v;
    }

    @Override
    public void onDestroy() {
        //if we were in edit mode, cancel it
        if(preferences.getBoolean(getString(R.string.editModePref),false))
            ma.serviceFinishEdit();
        preferences.edit()
            .putBoolean(getString(R.string.editModePref),false)
            .putString(getString(R.string.editModePos),"")
        .commit();

        super.onDestroy();
    }
    @Override
    public void onPause() {
        //if we are supposed to keep service, restart without the edit controls
        if(preferences.getBoolean(getString(R.string.editModePref),false)) {
            ma.serviceFinishEdit();
        }

        super.onPause();
    }
    @Override
    public void onResume() {
        if(preferences.getBoolean(getString(R.string.editModePref),false)) {
            ma.serviceStartEdit();
        }

        super.onResume();
    }
}
