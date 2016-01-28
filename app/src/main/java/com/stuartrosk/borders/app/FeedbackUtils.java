package com.stuartrosk.borders.app;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class FeedbackUtils extends Service {
    private static final String FEEDBACK_CHOOSER_TITLE = "Select feedback.";
    private static final String EMAIL_ADDRESS = "me@stuartrosk.com";

    public static void jumpToStore(Context context, SharedPreferences preferences) {
        openMarketLink(context);
        preferences.edit().putBoolean(context.getString(R.string.rate_us_pref),true).commit();
    }

    /*public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            if(packageName == "" || packageName == null) throw new PackageManager.NameNotFoundException();
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                throw new PackageManager.NameNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context,"Unable to find installed store package " + packageName, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return false;
        }
    }*/

    public static void openMarketLink(Context context) {
        String link = "";

        PackageManager pm = context.getPackageManager();
        String installer = pm.getInstallerPackageName(context.getApplicationContext().getPackageName());
        if(installer == null) {
            List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
            for (PackageInfo p : installedPackages) {
                if (p.packageName.contains("samsungapps")) {
                    installer = "samsung";
                    break;
                }
            }
        }

        if(installer == null) installer = "";
        installer = installer.toLowerCase();

        if(installer.contains("google") || installer.contains("android"))
        {
            link = "market://details?id=" + context.getString(R.string.pref_namespace);
        }
        else if(installer.contains("amazon"))
        {
            link = "amzn://apps/android?p=" + context.getString(R.string.pref_namespace);
        }
        else if(installer.contains("samsung"))
        {
            link = "samsungapps://ProductDetail/" + context.getString(R.string.pref_namespace);
        }

        if(link != "") {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(link));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Toast.makeText(context,"Unable to find installed store package.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static void askForFeedback(Context context) {
        /*final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, getFeedbackEmailAddress());
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getFeedbackEmailSubject(context));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getFeedbackDeviceInformation(context));
        context.startActivity(Intent.createChooser(emailIntent, FEEDBACK_CHOOSER_TITLE));*/

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

    //@Override
    protected void onCreate(Bundle savedInstanceState) {
        /*super.onCreate(savedInstanceState);

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

        finish();*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if(intent != null) {
            boolean showStore = intent.getAction() == "show_rating";
            boolean laterStore = intent.getAction() == "later_rating";
            boolean neverStore = intent.getAction() == "never_rating";
            SharedPreferences preferences = getSharedPreferences(getString(R.string.pref_namespace), MODE_PRIVATE);

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
        }

        this.stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}