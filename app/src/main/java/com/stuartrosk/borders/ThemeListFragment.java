package com.stuartrosk.borders;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;


public class ThemeListFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    ThemeListPreference lp;
    CustomListPreference cp;
    EditPrefListFragmentListener listener;
    SharedPreferences preferences;

    public interface EditPrefListFragmentListener {
        void onThemeSelectionChange();
    }

    public void updateEntries() {
        if(lp!=null)
            lp.setSummary("Current: " +
                preferences.
                getString(getString(R.string.theme_key), "Custom"));

        if(cp != null)
            cp.setSummary("Current: " +
                preferences.
                getString(getString(R.string.custom_key), getString(R.string.empty_custom_list_text)));

        if(preferences.getInt(getString(R.string.theme_id),2) == 1)
            showCustom();
        else
            hideCustom();

        listener.onThemeSelectionChange();
    }

    public void setThemeEntry(int index) {
        Log.d("ttest",String.valueOf(index));
        lp = (ThemeListPreference) findPreference("theme_list");
        lp.setResult(index);
        updateEntries();
    }
    public void setCustomEntry(int index) {
        Log.d("ctest",String.valueOf(index));
        cp = (CustomListPreference) findPreference("custom_list");
        cp.setResult(index);
        updateEntries();
    }

    public void hideCustom(){
        if(cp!=null)
            getPreferenceScreen().removePreference(cp);
    }

    public void showCustom(){
        if(cp != null)
            getPreferenceScreen().addPreference(cp);
    }

    public int refreshCustom(){
        ThemeJsonObject.Theme[] cThemes = ThemeJsonObject.getCustomThemes(getActivity());
        String[] cResourceNames = new String[cThemes.length];
        String[] cResourceValues = new String[cThemes.length];

        for (int i = 0; i < cThemes.length; ++i) {
            cResourceNames[i] = cThemes[i].title;
            cResourceValues[i] = String.valueOf(cThemes[i].id);
        }

        if (cp != null) {
            cp.setEntries(cResourceNames);
            cp.setEntryValues(cResourceValues);
        }

        updateEntries();

        return cThemes.length;
    }

    public int refreshThemes(){
        ThemeJsonObject.Theme[] themes = ThemeJsonObject.getThemes(getActivity());
        String[] resourceNames = new String[themes.length];
        String[] resourceValues = new String[themes.length];

        for(int i=0;i<themes.length;++i) {
            resourceNames[i] = themes[i].title;
            resourceValues[i] = String.valueOf(themes[i].id);
        }

        if(lp != null) {
            lp.setEntries(resourceNames);
            lp.setEntryValues(resourceValues);
        }

        return themes.length;
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

        preferences = getActivity().getSharedPreferences(getString(R.string.pref_namespace), getActivity().MODE_PRIVATE);
    }


    @Override
    public void onResume() {
        super.onResume();

        //setup themes list
        if(lp == null) {
            lp = (ThemeListPreference) findPreference("theme_list");
            lp.setOnPreferenceChangeListener(this);
            refreshThemes();
        }

        if(cp == null) {
            //setup custom list
            cp = (CustomListPreference) findPreference("custom_list");
            cp.setOnPreferenceChangeListener(this);
        }

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if(refreshCustom() < 1) {
            preferences.edit()
                .putString(getString(R.string.custom_key), getString(R.string.empty_custom_list_text))
            .commit();
        }
        updateEntries();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        updateEntries();

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        updateEntries();
    }
}
