package com.stuartrosk.borders.app;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsPrefFragment extends PreferenceFragment {

    SettingsPrefFragmentListener listener;

    public interface SettingsPrefFragmentListener {
        public void onFinishSettings();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (SettingsPrefFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SettingsPrefFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(getString(R.string.pref_namespace));
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.settings_pref_fragment);
    }

    @Override
    public void onStop() {
        listener.onFinishSettings();
        super.onStop();
    }

    @Override
    public void onPause() {
        listener.onFinishSettings();
        super.onPause();
    }


}