package com.stuartrosk.littleworlds.app;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class PreferenceListFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from XML resource
        this.addPreferencesFromResource(R.xml.preference_list_fragment);
    }


}
