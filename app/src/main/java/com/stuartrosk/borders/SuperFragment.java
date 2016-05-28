package com.stuartrosk.borders;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import com.google.android.gms.analytics.HitBuilders;
//import com.google.android.gms.analytics.Tracker;

public class SuperFragment extends Fragment {
    //private Tracker mTracker;
    private SharedPreferences preferences;
    private SuperFragmentListener listener;

    public interface SuperFragmentListener {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (SuperFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SuperFragmentListener");
        }
    }

    public void sendEvent(String category, String action){
        MainActivity.sendEvent(getActivity(), category, action, null, null);
    }
    public void sendEvent(String category, String action, String label){
        MainActivity.sendEvent(getActivity(),category, action, label, null);
    }
    public void sendEvent(String category, String action, String label, String value){
        MainActivity.sendEvent(getActivity(), category, action, label, value);
    }
    public void sendView(String optName) {
        MainActivity.sendView(getActivity(), optName);
    }

    @Override
    public void onResume() {
        super.onResume();

        sendView(getClass().getSimpleName());
    }
}
