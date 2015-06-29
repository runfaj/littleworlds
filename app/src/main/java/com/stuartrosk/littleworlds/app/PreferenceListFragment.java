package com.stuartrosk.littleworlds.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class PreferenceListFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    EditListPreference lp;

    public void updateThemeEntry() {
        lp = (EditListPreference) findPreference("theme_list");
        lp.setSummary("Current: Custom");
        lp.setSummary("Current: " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.theme_key), "Custom"));
    }
    public void updateThemeEntry(int index) {
        Log.d("test",String.valueOf(index));
        lp = (EditListPreference) findPreference("theme_list");
        lp.setResult(index);
        updateThemeEntry();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from XML resource
        this.addPreferencesFromResource(R.layout.preference_list_fragment);

        lp = (EditListPreference) findPreference("theme_list");
        lp.setOnPreferenceChangeListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updateThemeEntry();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String textValue = newValue.toString();

        EditListPreference listPreference = (EditListPreference) preference;
        CharSequence[] entries = listPreference.getEntries();
        int index = Integer.parseInt(textValue);

        if(index >= 0)
            Toast.makeText(preference.getContext(), entries[index], Toast.LENGTH_LONG);

        updateThemeEntry();

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        updateThemeEntry();
    }
}
