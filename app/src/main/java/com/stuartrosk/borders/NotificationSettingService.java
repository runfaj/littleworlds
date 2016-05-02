package com.stuartrosk.borders;

import android.annotation.TargetApi;
import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

@TargetApi(19)
public class NotificationSettingService extends NotificationListenerService {
    Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pack = sbn.getPackageName();
        String title = sbn.getNotification().extras.getString("android.title");// It will be select keyboard
        Log.d("notificationsetting","show "+title+" "+pack);
    }
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String pack = sbn.getPackageName();
        String title = sbn.getNotification().extras.getString("android.title");// It will be select keyboard
        Log.d("notificationsetting","hide "+title+" "+pack);
    }
}