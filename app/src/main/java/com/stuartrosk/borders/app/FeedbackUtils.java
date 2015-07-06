package com.stuartrosk.borders.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

public class FeedbackUtils {
    private static final String FEEDBACK_CHOOSER_TITLE = "Select feedback.";
    private static final String EMAIL_ADDRESS = "me@stuartrosk.com";

    public static boolean openApp(Context context, String packageName) {
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
            return false;
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
}