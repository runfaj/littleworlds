package com.stuartrosk.borders.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class autostart extends BroadcastReceiver
{
    private SharedPreferences preferences;

    public void onReceive(Context context, Intent arg1)
    {
        preferences = context.getSharedPreferences(context.getString(R.string.pref_namespace), context.MODE_PRIVATE);

        if(preferences.getBoolean(context.getString(R.string.service_enabled_pref),false)
                && preferences.getBoolean(context.getString(R.string.startup_pref),false)) {
            Intent intent = new Intent(context, WorldService.class);
            context.startService(intent);
        }
    }
}