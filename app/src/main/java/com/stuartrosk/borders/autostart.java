package com.stuartrosk.borders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


public class autostart extends BroadcastReceiver
{

    public void onReceive(Context context, Intent arg1)
    {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.pref_namespace), context.MODE_PRIVATE);

        if(preferences.getBoolean(context.getString(R.string.service_enabled_pref),false)
                && preferences.getBoolean(context.getString(R.string.startup_pref),false)) {
            Intent intent = new Intent(context, WorldService.class);
            context.startService(intent);
        }
    }
}