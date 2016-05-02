package com.stuartrosk.borders;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


public class SettingsPrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    SettingsPrefFragmentListener listener;

    public interface SettingsPrefFragmentListener {
        void onFinishSettings();
        void showServiceNotification();
        void onStartupPrefChecked();
        boolean isAccessibilityEnabled();
        void onAccessibilitySettingChecked();
        //void onNotificationSettingChecked();
        //boolean isNotificationSettingEnabled();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_pref_fragment);
    }

    public void checkUnlockedPref(){
        if(getPreferenceManager().getSharedPreferences().getBoolean(getString(R.string.unlocked_pref),false)) {
            UnlockDialog unlockDialog = (UnlockDialog) findPreference("pref_screen_unlock");
            PreferenceCategory mCategory = (PreferenceCategory) findPreference("pref_screen_settings");
            mCategory.removePreference(unlockDialog);
        }
    }

    public void checkNotifIconPref(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            CheckBoxPreference pref = (CheckBoxPreference) findPreference(getString(R.string.notification_icon_pref));
            pref.setEnabled(false);
        }
    }

    public void checkKeyboardPref(){
        /*if(!listener.isNotificationSettingEnabled()) {
            setKeyboardPref(false);
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
        }*/
        setKeyboardPref(false);
        CheckBoxPreference keyboard = (CheckBoxPreference)findPreference(getString(R.string.system_keyboard_pref));
        keyboard.setEnabled(false);
    }

    public void setStartupPref(boolean checked){
        getPreferenceManager().getSharedPreferences().edit().putBoolean(getString(R.string.startup_pref), checked).commit();
        CheckBoxPreference pref = (CheckBoxPreference) findPreference(getString(R.string.startup_pref));
        pref.setChecked(checked);
    }

    public void setKeyboardPref(boolean checked){
        getPreferenceManager().getSharedPreferences().edit().putBoolean(getString(R.string.system_keyboard_pref), checked).commit();
        CheckBoxPreference pref = (CheckBoxPreference) findPreference(getString(R.string.system_keyboard_pref));
        pref.setChecked(checked);
    }

    public void setPulldownPref(boolean checked){
        getPreferenceManager().getSharedPreferences().edit().putBoolean(getString(R.string.system_pulldown_pref), checked).commit();
        CheckBoxPreference pref = (CheckBoxPreference) findPreference(getString(R.string.system_pulldown_pref));
        pref.setChecked(checked);
    }

    public void setVersionPref(){
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String versionName = pInfo.packageName + " - " + pInfo.versionName + " - " + pInfo.versionCode;
            Preference aboutPref = findPreference("pref_about");
            aboutPref.setSummary(versionName);
        } catch (Exception e) {}
    }

    @Override
    public void onStop() {
        listener.onFinishSettings();
        super.onStop();
    }

    @Override
    public void onResume() {
        ////////////////////////////////////////////////////////////////////////////////////want an easter egg for taps on version number
        /////////////////////////////////////////////////////this would give a popup of "the cake is a lie" with an emoticon
        /////////////////////////////////////////////////////then unlock a custom cake theme
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        setVersionPref();

        if(!listener.isAccessibilityEnabled()) {
            setPulldownPref(false);
        }

        try {
            checkUnlockedPref();
            checkNotifIconPref();
            checkKeyboardPref();
        } catch (Exception e) {}
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
        if(key.equals(getString(R.string.notification_pref))
                || key.equals(getString(R.string.notification_icon_pref))) {
            listener.showServiceNotification();
        }
        if(key.equals(getString(R.string.startup_pref))) {
            listener.onStartupPrefChecked();
        }
        if((key.equals(getString(R.string.system_pulldown_pref)))
                && sharedPreferences.getBoolean(key,false)){
            listener.onAccessibilitySettingChecked();
        }
        /*if(key.equals(getString(R.string.system_keyboard_pref))
                && sharedPreferences.getBoolean(key,false)){
            listener.onNotificationSettingChecked();
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if(v != null) {
            ListView lv = (ListView) v.findViewById(android.R.id.list);
            Resources r = getResources();
            int pxSide = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());
            int pxTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());
            int pxBottom = pxTop*2;
            lv.setPadding(pxSide, pxTop, pxSide, pxBottom);
        }
        return v;
    }
}