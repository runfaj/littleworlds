package com.stuartrosk.littleworlds.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity
    implements EditFragment.EditFragmentListener,
        HomeFragment.HomeFragmentListener,
        ImageEditFragment.ImageEditFragmentListener,
        EditPrefListFragment.EditPrefListFragmentListener {

    private HomeFragment fragmentHome;
    private EditFragment fragmentEdit;
    private SharedPreferences preferences;

    /** service functions **/

    public boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WorldService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startWorldService(boolean editMode, String editPos) {
        if(!isServiceRunning()) {
            Intent worldService = new Intent(getApplicationContext(), WorldService.class);
            worldService.putExtra("editMode",editMode);
            worldService.putExtra("editPos",editPos);
            startService(worldService);
        }
    }

    public void stopWorldService() {
        if(isServiceRunning())
            stopService(new Intent(getApplicationContext(), WorldService.class));
    }

    /** edit screen functions **/

    public void showEditScreen() {
        getFragmentManager().beginTransaction()
            .replace(R.id.mainFrame, fragmentEdit, "E")
            .addToBackStack(null)
        .commit();
        onStartEdit();
    }

    public void hideEditScreen() {
        getFragmentManager().popBackStack();
        onFinishEdit();
    }

    @Override
    public void onFinishEdit() {
        stopWorldService();
        if(preferences.getBoolean(getString(R.string.service_enabled_pref),false))
            startWorldService(false, "");
    }

    @Override
    public void onStartEdit() {
        stopWorldService();
        startWorldService(true, "");
    }

    @Override
    public void onThemeSelectionChange() {
        EditFragment testFragment = (EditFragment)getFragmentManager().findFragmentByTag("E");
        if(testFragment != null && testFragment.isVisible()) {
            stopWorldService();
            startWorldService(true, "");
        }
    }

    /** image edit screen **/

    public void showImageEditScreen(String editPos) {
        getFragmentManager().beginTransaction()
            .replace(R.id.mainFrame, ImageEditFragment.newInstance(editPos), "IE")
            .addToBackStack(null)
        .commit();

        fragmentEdit.updatePreferenceList(1); //force theme to be "custom"
        onStartImageEdit(editPos);
    }

    public void cancelImageEditScreen() {
        //////// are you sure dialog?
        hideImageEditScreen();
        onFinishImageEdit();
    }

    public void hideImageEditScreen() {
        getFragmentManager().popBackStack();
        stopWorldService();
        startWorldService(true, "");
    }

    @Override
    public void onFinishImageEdit() {
        stopWorldService();
        if(preferences.getBoolean(getString(R.string.service_enabled_pref),false))
            startWorldService(false, "");
    }

    @Override
    public void onStartImageEdit(String editPos) {
        stopWorldService();
        startWorldService(true, editPos);
    }

    /** main activity functions **/

    @Override
    public void firstTimer() {
        preferences.edit()
            .putBoolean(getString(R.string.first_time_pref), false)
            .putBoolean(getResources().getString(R.string.service_enabled_pref),true)
            .putInt(getString(R.string.theme_id), 2)
            .putString(getString(R.string.theme_key), "Cats")
        .commit();
        Log.d("testing", "balalda");
        stopWorldService();
        startWorldService(false,"");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentHome = new HomeFragment();
        fragmentEdit = new EditFragment();

        getFragmentManager().beginTransaction()
            .add(R.id.mainFrame, fragmentHome, "H")
        .commit();

        preferences = getSharedPreferences("com.stuartrosk.littleworlds",MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
    @Override
     protected void onDestroy() {
        //if we are supposed to keep service, restart without the edit controls
        if(preferences.getBoolean("starting_edit",false)) {
            preferences.edit().putBoolean("starting_edit",false).commit();
        } else {
            if (!preferences.getBoolean("serviceEnabled", false)) {
                stopService(new Intent(getApplicationContext(), WorldService.class));
            } else {
                Intent i = new Intent(getApplicationContext(), WorldService.class);
                stopService(i);
                startService(i);
            }
        }

        super.onDestroy();
    }
    @Override
    protected void onPause() {
        //if we are supposed to keep service, restart without the edit controls
        if(preferences.getBoolean("starting_edit",false)) {
            preferences.edit().putBoolean("starting_edit",false).commit();
        } else {
            if (!preferences.getBoolean("serviceEnabled", false)) {
                stopService(new Intent(getApplicationContext(), WorldService.class));
            } else {
                Intent i = new Intent(getApplicationContext(), WorldService.class);
                stopService(i);
                startService(i);
            }
        }

        super.onPause();
    }
    @Override
    protected void onResume() {
        //always just restart service with edit controls when resuming this screen
        Intent i = new Intent(getApplicationContext(), WorldService.class);
        stopService(i);
        i.putExtra("hideEditMode","true");
        startService(i);

        super.onResume();
    }
     */
}
