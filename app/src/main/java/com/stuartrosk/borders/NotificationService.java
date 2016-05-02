package com.stuartrosk.borders;

import android.app.*;
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
        if(!isServiceRunning()
                && !preferences.getBoolean(getString(R.string.service_enabled_pref),false)){
            Log.d("service","starting service");
            preferences.edit()
                    .putBoolean(getString(R.string.service_enabled_pref),true)
                    .putBoolean(getString(R.string.service_editmode),editMode)
                    .putString(getString(R.string.service_editpos),editPos)
                    .commit();
            Intent worldService = new Intent(getApplicationContext(), WorldService.class);
            worldService.putExtra("editMode",editMode);
            worldService.putExtra("editPos",editPos);
            startService(worldService);
        }
    }

    public void stopWorldService() {
        Log.d("service","stopping service");
        if (isServiceRunning() || preferences.getBoolean(getString(R.string.service_enabled_pref),false)) {
            preferences.edit()
                    .putBoolean(getString(R.string.service_enabled_pref),false)
                    .putBoolean(getString(R.string.service_editmode),false)
                    .putString(getString(R.string.service_editpos),"")
                    .commit();
            stopService(new Intent(getApplicationContext(), WorldService.class));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences(getString(R.string.pref_namespace),MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Boolean startBordersExtra = false,
                stopBordersExtra = false,
                stopServiceExtra = false;

        if(intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                stopBordersExtra = extras.getBoolean(getString(R.string.stopBordersExtra),false);
                startBordersExtra = extras.getBoolean(getString(R.string.startBordersExtra),false);
                stopServiceExtra = extras.getBoolean(getString(R.string.stop_notif_extra), false);
            }
        }

        Log.d("service","notification onstartcommand");
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

        showServiceNotification(stopServiceExtra);

        return START_NOT_STICKY;
    }

    private void setToggleSwitch(boolean state) {
        Log.d("service","notif settoggleswitch " + state);
        preferences.edit().putBoolean(getString(R.string.service_toggled_pref), state).commit();
        Intent intent = new Intent("toggle-switch");
        intent.putExtra("toggle", state);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void showServiceNotification(boolean forceStop) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Log.d("service","notif bool: "+Boolean.toString(forceStop)+" "+Boolean.toString(preferences.getBoolean(getString(R.string.notification_pref), true)));
        if (!forceStop && preferences.getBoolean(getString(R.string.notification_pref), true)) {
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

            int iconId = R.drawable.app_notif_icon;
            int toggleId = R.drawable.ic_stop_white_48dp;
            String currState = "Stop";
            String currStateTitle = "Running";
            String currStateTitle2 = "stop";


            if (!preferences.getBoolean(getString(R.string.service_enabled_pref), false)) {
                currState = "Start";
                currStateTitle = "Stopped";
                currStateTitle2 = "Start";
                toggleId = R.drawable.ic_play_arrow_white_48dp;
            }


            NotificationCompat.Builder n = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(currStateTitle + ", touch to " + currStateTitle2 + " " + getString(R.string.app_name))
                .setSmallIcon(iconId)
                .setContentIntent(mainIntent)
                .setAutoCancel(false)
                .setOngoing(true);

            if(preferences.getBoolean(getString(R.string.notification_icon_pref),false))
                n.setPriority(Notification.PRIORITY_MIN);
            else
                n.setPriority(Notification.PRIORITY_DEFAULT);

            if (!preferences.getBoolean(getString(R.string.service_enabled_pref), false)) {
                n.addAction(toggleId, currState, startWorldService);
            } else {
                n.addAction(toggleId, currState, stopWorldService);
            }

            if (!preferences.getBoolean(getString(R.string.unlocked_pref),false)) {
                Intent unlock = new Intent(this, MainActivity.class);
                unlock.putExtra(getString(R.string.unlockAppExtra), true);
                PendingIntent unlockApp = PendingIntent.getActivity(this, 0, unlock, PendingIntent.FLAG_UPDATE_CURRENT);
                n.addAction(R.drawable.ic_lock_open_white_48dp, "Unlock", unlockApp);
            }

            //n.addAction(R.drawable.ic_get_app_black_48dp, "???", mainIntent); //////////////////////change this

            notificationManager.notify(R.integer.service_notification_id, n.build());
        } else {
            notificationManager.cancel(R.integer.service_notification_id);
            stopSelf();
        }
    }
}
