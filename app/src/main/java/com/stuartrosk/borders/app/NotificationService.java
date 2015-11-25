package com.stuartrosk.borders.app;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NotificationService extends Service {
    public class LocalBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }
    private final LocalBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    SharedPreferences preferences;
    Boolean startBordersExtra = false,
            stopBordersExtra = false;

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

    public void stopWorldService() {
        if(isServiceRunning())
            stopService(new Intent(getApplicationContext(), WorldService.class));
        showServiceNotification();
    }


    @Override
    public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences(getString(R.string.pref_namespace),MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if(intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                stopBordersExtra = extras.getBoolean(getString(R.string.stopBordersExtra),false);
                startBordersExtra = extras.getBoolean(getString(R.string.startBordersExtra),false);
            }
        }

        if(startBordersExtra) {
            startWorldService(false, "");
            preferences.edit().putBoolean(getString(R.string.service_enabled_pref),true).commit();
            setToggleSwitch(true);
        }
        if(stopBordersExtra) {
            stopWorldService();
            preferences.edit().putBoolean(getString(R.string.service_enabled_pref),false).commit();
            setToggleSwitch(false);
        }

        showServiceNotification();

        return START_NOT_STICKY;
    }

    private void setToggleSwitch(boolean state) {
        Intent intent = new Intent("toggle-switch");
        intent.putExtra("toggle", state);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void showServiceNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (preferences.getBoolean(getString(R.string.notification_pref), true)
            && !preferences.getBoolean(getString(R.string.edit_mode_pref),false)) {
            //main intent for just clicking on notification
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent mainIntent = PendingIntent.getActivity(this, 0, intent, 0);

            //notification "play" button
            Intent newWorldService = new Intent(this, NotificationService.class);
            newWorldService.putExtra(getString(R.string.startBordersExtra), true);
            newWorldService.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent startWorldService = PendingIntent.getService(this, 555, newWorldService, 0);

            //notification "stop" button
            Intent oldWorldService = new Intent(this, NotificationService.class);
            oldWorldService.putExtra(getString(R.string.stopBordersExtra), true);
            oldWorldService.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent stopWorldService = PendingIntent.getService(this, 556, oldWorldService, 0);

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

            if (!preferences.getBoolean(getString(R.string.service_enabled_pref), false)) {
                n.addAction(toggleId, currState, startWorldService);
            } else {
                n.addAction(toggleId, currState, stopWorldService);
            }

            //n.addAction(R.drawable.ic_get_app_black_48dp, "???", mainIntent); //////////////////////change this

            notificationManager.notify(R.integer.service_notification_id, n.build());
        } else {
            notificationManager.cancel(R.integer.service_notification_id);
        }
    }
}
