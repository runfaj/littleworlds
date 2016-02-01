package com.stuartrosk.borders.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class CustomListFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    CustomListPreference lp;
    CustomListFragmentListener listener;

    public interface CustomListFragmentListener {
        public void onCustomSelectionChange(); //////////////////////////////////////////////////////////////////////////
    }

    public void updateThemeEntry() {
        lp = (CustomListPreference) findPreference("custom_list");
        lp.setSummary("Current: (None)");
        lp.setSummary("Current: " +
                getActivity().
                        getSharedPreferences(getString(R.string.pref_namespace), getActivity().MODE_PRIVATE).
                        getString(getString(R.string.custom_key)
                                , "(None)"));

        listener.onCustomSelectionChange();
    }

    public void updateThemeEntry(int id) {
        Log.d("test",String.valueOf(id));
        lp = (CustomListPreference) findPreference("custom_list");
        lp.setResult(id);
        updateThemeEntry();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (CustomListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CustomListFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from XML resource
        this.addPreferencesFromResource(R.xml.custom_list_fragment);

        lp = (CustomListPreference) findPreference("custom_list");
        lp.setOnPreferenceChangeListener(this);

        ThemeJsonObject.Theme[] themes = ThemeJsonObject.getThemes(getActivity());
        String[] resourceNames = new String[themes.length];
        String[] resourceValues = new String[themes.length];

        for(int i=0;i<themes.length;++i) {
            resourceNames[i] = themes[i].title;
            resourceValues[i] = String.valueOf(themes[i].id);
        }

        lp.setEntries(resourceNames);
        lp.setEntryValues(resourceValues);
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
        updateThemeEntry();

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        updateThemeEntry();
    }
}
