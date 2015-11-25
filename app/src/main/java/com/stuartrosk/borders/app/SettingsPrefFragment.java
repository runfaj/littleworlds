package com.stuartrosk.borders.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SettingsPrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    SettingsPrefFragmentListener listener;

    public interface SettingsPrefFragmentListener {
        public void onFinishSettings();
        public void showServiceNotification();
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

        checkUnlockedPref();
    }

    public void checkUnlockedPref(){
        if(getPreferenceManager().getSharedPreferences().getBoolean(getString(R.string.unlocked_pref),false)) {
            UnlockDialog unlockDialog = (UnlockDialog) findPreference("pref_screen_unlock");
            PreferenceCategory mCategory = (PreferenceCategory) findPreference("pref_screen_extras");
            mCategory.removePreference(unlockDialog);
        }
    }

    @Override
    public void onStop() {
        listener.onFinishSettings();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        listener.onFinishSettings();
        super.onPause();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
        if(key.equals(getString(R.string.notification_pref))
                || key.equals(getString(R.string.notification_icon_pref))) {
            listener.showServiceNotification();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if(v != null) {
            ListView lv = (ListView) v.findViewById(android.R.id.list);
            Resources r = getResources();
            int pxSide = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());
            int pxTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());
            lv.setPadding(pxSide, pxTop, pxSide, pxTop);

        }
        return v;
    }
}