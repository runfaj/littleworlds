package com.stuartrosk.borders.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/*****
 *
 * TODO:
 * Fix sharing since it isn't including all apps (like email)
 * Add start service/notification on boot
 * Hook up notification to toggle switch when app is open
 * Don't close existing open activity if open and notification is clicked
 * Add reset option for image edit
 * Add feedback popup to settings
 * Add app info to settings
 * test landscape mode
 * Implement free/paid features:
 *   locked custom areas
 *   locked themes
 *   unlock app and signed code checking
 * material design
 * images images images
 * notifications for rating
 * notifications for unlocking
 * ads in free version
 *
 * MAYBE TODO:
 * Add photo cropping and save to new folder
 * First timer tutorial
 *
 */


public class MainActivity extends Activity
    implements EditFragment.EditFragmentListener,
        HomeFragment.HomeFragmentListener,
        ImageEditFragment.ImageEditFragmentListener,
        EditPrefListFragment.EditPrefListFragmentListener,
        SettingsPrefFragment.SettingsPrefFragmentListener {

    private HomeFragment fragmentHome;
    private EditFragment fragmentEdit;
    private SharedPreferences preferences;
    private SettingsPrefFragment fragmentSettings;

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
        showServiceNotification();
    }

    public void startScreenshotWorldService() {
        stopWorldService();
        if(!isServiceRunning()) {
            Intent worldService = new Intent(getApplicationContext(), WorldService.class);
            worldService.putExtra("screenshot",true);
            startService(worldService);
        }
    }

    public void stopScreenshotWorldService() {
        stopWorldService();
        if(preferences.getBoolean(getString(R.string.service_enabled_pref),false))
            startWorldService(false, "");
    }

    public void stopWorldService() {
        if(isServiceRunning())
            stopService(new Intent(getApplicationContext(), WorldService.class));
        showServiceNotification();
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
        boolean editMode = false;
        if(preferences.getInt(getString(R.string.theme_id),1) == 1)
            editMode = true;
        stopWorldService();
        startWorldService(editMode, "");
    }

    @Override
    public void onThemeSelectionChange() {
        boolean editMode = false;
        if(preferences.getInt(getString(R.string.theme_id),1) == 1)
            editMode = true;
        EditFragment testFragment = (EditFragment)getFragmentManager().findFragmentByTag("E");
        if(testFragment != null && testFragment.isVisible()) {
            stopWorldService();
            startWorldService(editMode, "");
        }
        if(fragmentEdit != null && fragmentEdit.isAdded() && fragmentEdit.isVisible()) {
            fragmentEdit.toggleEditIcons();
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

    /** settings functions **/

    public void showSettings() {
        getFragmentManager().beginTransaction()
            .replace(R.id.mainFrame, fragmentSettings, "S")
            .addToBackStack(null)
        .commit();
    }

    public void hideSettings() {
        getFragmentManager().popBackStack();
        onFinishSettings();
    }

    public void onFinishSettings() {
        showServiceNotification();
    }

    /** home fragment functions **/

    @Override
    public void firstTimer() {
        preferences.edit()
            .putBoolean(getString(R.string.first_time_pref), false)
            .putBoolean(getResources().getString(R.string.service_enabled_pref),true)
            .putInt(getString(R.string.theme_id), 2)
            .putString(getString(R.string.theme_key), "Cats")
        .commit();
        stopWorldService();
        startWorldService(false,"");
    }

    /** main activity functions **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentHome = new HomeFragment();
        fragmentEdit = new EditFragment();
        fragmentSettings = new SettingsPrefFragment();

        getFragmentManager().beginTransaction()
            .add(R.id.mainFrame, fragmentHome, "H")
        .commit();

        preferences = getSharedPreferences(getString(R.string.pref_namespace),MODE_PRIVATE);

        Boolean stopBordersExtra = getIntent().getBooleanExtra(getString(R.string.stopBordersExtra),false);
        Boolean startBordersExtra = getIntent().getBooleanExtra(getString(R.string.startBordersExtra),false);

        Log.d("borders",Boolean.toString(stopBordersExtra) + " " + Boolean.toString(startBordersExtra));

        if(startBordersExtra) {
            Log.d("starting","yup");
            startWorldService(false, "");
            preferences.edit().putBoolean(getString(R.string.service_enabled_pref),true).commit();
            showServiceNotification();
            finish();
        }
        if(stopBordersExtra) {
            Log.d("stopping","yup");
            stopWorldService();
            preferences.edit().putBoolean(getString(R.string.service_enabled_pref),false).commit();
            showServiceNotification();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(screenshotReceiver,
                new IntentFilter("service-ready"));
    }

    private BroadcastReceiver screenshotReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("testing","got intent");
            fragmentHome.onScreenshotReady();
        }
    };

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(screenshotReceiver);
        super.onPause();
    }

    private void showServiceNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(preferences.getBoolean(getString(R.string.notification_pref),true)) {
            //main intent for just clicking on notification
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent mainIntent = PendingIntent.getActivity(this, 0, intent, 0);

            //notification "play" button
            Intent newWorldService = new Intent(this, MainActivity.class);
            newWorldService.putExtra(getString(R.string.startBordersExtra),true);
            newWorldService.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent startWorldService = PendingIntent.getActivity(this, 555, newWorldService, 0);

            //notification "stop" button
            Intent oldWorldService = new Intent(this, MainActivity.class);
            oldWorldService.putExtra(getString(R.string.stopBordersExtra),true);
            oldWorldService.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent stopWorldService = PendingIntent.getActivity(this, 556, oldWorldService, 0);

            int iconId = android.R.color.transparent;
            int toggleId = R.drawable.ic_stop_white_48dp;
            String currState = "Stop";
            String currStateTitle = "Running";
            String currStateTitle2 = "stop";


            if (preferences.getBoolean("notification_icon_pref", true)) {
                iconId = R.drawable.ic_image_black_48dp;
            }
            if (!preferences.getBoolean(getString(R.string.service_enabled_pref), false)) {
                currState = "Start";
                currStateTitle = "Stopped";
                currStateTitle2 = "Start";
                toggleId = R.drawable.ic_play_arrow_white_48dp;
            }


            NotificationCompat.Builder n = new NotificationCompat.Builder(this)
                    .setContentTitle("Borders")
                    .setContentText(currStateTitle + ", touch to " + currStateTitle2 + ".")
                    .setSmallIcon(iconId)
                    .setContentIntent(mainIntent)
                    .setAutoCancel(false)
                    .setOngoing(true);

            if(!preferences.getBoolean(getString(R.string.service_enabled_pref), false)) {
                Log.d("notif_start","here");
                n.addAction(toggleId, currState, startWorldService);
            } else {
                Log.d("notif_end","here");
                n.addAction(toggleId, currState, stopWorldService);
            }

            //n.addAction(R.drawable.ic_get_app_black_48dp, "???", mainIntent); //////////////////////change this

            notificationManager.notify(R.integer.service_notification_id, n.build());
        } else {
            notificationManager.cancel(R.integer.service_notification_id);
        }

        ////////////change title and play/stop based on pref
    }

    private void showUnlockNotification() {

    }

    private void showRatingNotification() {

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
