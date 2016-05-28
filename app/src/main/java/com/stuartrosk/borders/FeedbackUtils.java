package com.stuartrosk.borders;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;


public class FeedbackUtils extends Service {
    private static final String FEEDBACK_CHOOSER_TITLE = "Select feedback.";
    private static final String EMAIL_ADDRESS = "me@stuartrosk.com";

    public static void openGooglePlusPage(Activity context){
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/communities/117472363584237491157")));
        /*Intent i = new Intent(Intent.ACTION_VIEW);
        i.setClassName("com.google.android.apps.plus", "com.google.android.apps.plus.phone.UrlGatewayActivity");
        i.putExtra("customAppUri", "117472363584237491157");
        context.startActivity(i);

        checkIfAppExists(context, i, "Google Plus");*/
    }

    // method to check whether an app exists or not
    public static void checkIfAppExists(Context context, Intent appIntent, String appName){
        if (appIntent.resolveActivity(context.getPackageManager()) != null) {
            // start the activity if the app exists in the system
            context.startActivity(appIntent);
        } else {
            Toast.makeText(context, appName + " app does not exist!", Toast.LENGTH_LONG).show();
        }
    }

    public static void jumpToStore(Context context, SharedPreferences preferences) {
        preferences.edit().putBoolean(context.getString(R.string.rate_us_pref),true).commit();
        openMarketLink(context);
    }

    public static void openMarketLink(Context context) {
        MainActivity.storeJump(context, context.getString(R.string.pref_namespace));
    }

    public static void askForFeedback(Context context) {
        final Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, getFeedbackEmailAddress());
        intent.putExtra(Intent.EXTRA_SUBJECT, getFeedbackEmailSubject(context));
        intent.putExtra(Intent.EXTRA_TEXT, getFeedbackDeviceInformation(context));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
           context.startActivity(Intent.createChooser(intent, FEEDBACK_CHOOSER_TITLE));
        }
    }

    private static String[] getFeedbackEmailAddress() {
        return new String[] { EMAIL_ADDRESS };
    }

    private static String getFeedbackEmailSubject(Context context) {
        return getApplicationName(context) + " Feedback v" + getAppVersion(context);
    }

    private static String getApplicationName(Context context) {
        return context.getString(context.getApplicationInfo().labelRes);
    }

    private static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            return "vX.XX";
        }
    }

    private static String getFeedbackDeviceInformation(Context context) {
        StringBuilder emailMessage = new StringBuilder();
        emailMessage.append("\n\n_________________");
        emailMessage.append("\n\nDevice info:\n\n");
        emailMessage.append(getHandsetInformation(context));
        emailMessage.append("\nPlease leave this data in the email to help with app issues and write above or below here. \n\n");
        emailMessage.append("_________________\n\n");
        return emailMessage.toString();
    }

    private static String getHandsetInformation(Context context) {
        StringBuilder handsetInfoBuilder = new StringBuilder();
        handsetInfoBuilder.append("Bootloader: " + Build.BOOTLOADER);
        handsetInfoBuilder.append("\nBrand: " + Build.BRAND);
        handsetInfoBuilder.append("\nDevice: " + Build.DEVICE);
        handsetInfoBuilder.append("\nManufacturer: " + Build.MANUFACTURER);
        handsetInfoBuilder.append("\nModel: " + Build.MODEL);
        handsetInfoBuilder.append("\nScreen Density: " + getDeviceDensity(context));
        handsetInfoBuilder.append("\nVersion SDK int: " + Build.VERSION.SDK_INT);
        handsetInfoBuilder.append("\nVersion codename: " + Build.VERSION.CODENAME);
        handsetInfoBuilder.append("\nVersion incremental: " + Build.VERSION.INCREMENTAL);
        handsetInfoBuilder.append("\n");
        return handsetInfoBuilder.toString();
    }

    private static float getDeviceDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showStore = getIntent().getAction() == "show_rating";
        boolean laterStore = getIntent().getAction() == "later_rating";
        boolean neverStore = getIntent().getAction() == "never_rating";
        SharedPreferences preferences = getSharedPreferences(getString(R.string.pref_namespace),MODE_PRIVATE);

        //Toast.makeText(getApplicationContext(),"show: "+showStore+" later: "+laterStore+" never: "+neverStore,Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(),getIntent().getAction(),Toast.LENGTH_LONG).show();

        if(neverStore)
            preferences.edit().putBoolean(getString(R.string.never_rate_pref),true).commit();
        else if(showStore && !preferences.getBoolean(getString(R.string.never_rate_pref),false)) {
            jumpToStore(getApplicationContext(), preferences);
            preferences.edit().putBoolean(getString(R.string.never_rate_pref),true).commit();
        } else if (laterStore) {
            //do nothing since the dialog auto cancels
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(R.integer.rating_notification_id);

        finish();
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        /** ga part **/
        MainActivity.sendView(getApplicationContext(),getClass().getSimpleName());
        /****/

        if(intent != null) {
            boolean showStore = intent.getAction().equals("show_rating");
            boolean laterStore = intent.getAction().equals("later_rating");
            boolean neverStore = intent.getAction().equals("never_rating");
            SharedPreferences preferences = getSharedPreferences(getString(R.string.pref_namespace), MODE_PRIVATE);

            if(neverStore) {
                MainActivity.sendEvent(getApplicationContext(),"Notification", "Rate Never",null,null);
                preferences.edit().putBoolean(getString(R.string.never_rate_pref), true).commit();
            } else if(showStore && !preferences.getBoolean(getString(R.string.never_rate_pref),false)) {
                MainActivity.sendEvent(getApplicationContext(),"Notification","Rate",null,null);
                jumpToStore(getApplicationContext(), preferences);
                preferences.edit().putBoolean(getString(R.string.never_rate_pref),true).commit();
            } else if (laterStore) {
                MainActivity.sendEvent(getApplicationContext(),"Notification","Rate Later",null,null);
                //do nothing since the dialog auto cancels
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(R.integer.rating_notification_id);
        }

        this.stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}