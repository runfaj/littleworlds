package com.stuartrosk.borders;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;


public class BordersAccessibilityService extends AccessibilityService {

    static final String TAG = "AccessibilityService";

    private boolean systemBarDown = false;
    private SharedPreferences preferences;

    private String getEventType(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "TYPE_VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "TYPE_VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "TYPE_VIEW_SELECTED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "TYPE_VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "TYPE_VIEW_TEXT_CHANGED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "TYPE_WINDOW_STATE_CHANGED";
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                return "TYPE_VIEW_HOVER_ENTER";
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                return "TYPE_VIEW_HOVER_EXIT";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_START";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_END";
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                return "TYPE_WINDOW_CONTENT_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                return "TYPE_VIEW_SCROLLED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                return "TYPE_VIEW_TEXT_SELECTION_CHANGED";
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                return "TYPE_ANNOUNCEMENT";
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                return "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                return "TYPE_GESTURE_DETECTION_START";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                return "TYPE_GESTURE_DETECTION_END";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                return "TYPE_TOUCH_INTERACTION_START";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                return "TYPE_TOUCH_INTERACTION_END";
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                return "TYPE_WINDOWS_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
                return "TYPE_VIEW_CONTEXT_CLICKED";
        }

        return "default";
    }

    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s);
        }
        return sb.toString();
    }

    public boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WorldService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startWorldService() {
        if(!isServiceRunning()
                && !preferences.getBoolean(getString(R.string.service_enabled_pref),false)){
            Log.d("service","starting service accessibility");
            preferences.edit()
                .putBoolean(getString(R.string.service_enabled_pref),true)
            .commit();
            Intent worldService = new Intent(getApplicationContext(), WorldService.class);
            worldService.putExtra("editMode",preferences.getBoolean(getString(R.string.service_editmode),false));
            worldService.putExtra("editPos",preferences.getString(getString(R.string.service_editpos),"none"));
            startService(worldService);
        }
    }

    public void stopWorldService() {
        Log.d("service","stopping service");
        if (isServiceRunning() || preferences.getBoolean(getString(R.string.service_enabled_pref),false)) {
            preferences.edit()
                .putBoolean(getString(R.string.service_enabled_pref),false)
            .commit();
            stopService(new Intent(getApplicationContext(), WorldService.class));
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        /*Log.v(TAG, String.format(
                "onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
                getEventType(event), event.getClassName(), event.getPackageName(),
                event.getEventTime(), getEventText(event)));//**/

        //track pulldown of system bar
        if(preferences.getBoolean(getString(R.string.service_toggled_pref),false)
            && preferences.getBoolean(getString(R.string.system_pulldown_pref),false)
            && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            if (!systemBarDown && (
                    (
                        Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2
                        && getEventText(event).toLowerCase().contains("notification shade")
                    )
                    || (
                        Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2
                        && event.getClassName().equals("android.widget.FrameLayout")
                        && event.getPackageName().equals("com.android.systemui")
                    )
                )
            ) {
                systemBarDown = true;
                stopWorldService();
            }

            if (systemBarDown && (
                    (
                        Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2
                        && !getEventText(event).toLowerCase().contains("notification shade")
                    )
                    || (
                        Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2
                        && !(event.getClassName().equals("android.widget.FrameLayout")
                            && event.getPackageName().equals("com.android.systemui"))
                    )
                )
            ) {
                systemBarDown = false;
                startWorldService();
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.v(TAG, "onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        Log.v(TAG, "onServiceConnected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        setServiceInfo(info);

        preferences = getSharedPreferences(getString(R.string.pref_namespace),MODE_PRIVATE);
    }

}
