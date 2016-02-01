package com.stuartrosk.borders.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class ThemeListFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    ThemeListPreference lp;
    EditPrefListFragmentListener listener;

    public interface EditPrefListFragmentListener {
        public void onThemeSelectionChange();
    }

    public void updateThemeEntry() {
        lp = (ThemeListPreference) findPreference("theme_list");
        lp.setSummary("Current: Custom");
        lp.setSummary("Current: " +
                getActivity().
                    getSharedPreferences(getString(R.string.pref_namespace), getActivity().MODE_PRIVATE).
                    getString(getString(R.string.theme_key)
                        , "Custom"));

        listener.onThemeSelectionChange();
    }
    public void updateThemeEntry(int index) {
        Log.d("test",String.valueOf(index));
        lp = (ThemeListPreference) findPreference("theme_list");
        lp.setResult(index);
        updateThemeEntry();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (EditPrefListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PrefListFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from XML resource
        this.addPreferencesFromResource(R.xml.edit_pref_list_fragment);

        lp = (ThemeListPreference) findPreference("theme_list");
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
