package com.stuartrosk.borders;

import android.Manifest;
import android.animation.Animator;
import android.annotation.TargetApi; //don't delete, this is actually used
import android.app.*;
import android.app.FragmentManager;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.*;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.simplifynowsoftware.androidmarketmanager.AMMConstants;
import com.simplifynowsoftware.androidmarketmanager.AMMLinks;
import com.tapjoy.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Things to change when switching between app type:
 * app name in strings
 * ---auto items if proper package rename---
 * rename package
 * top of manifest
 * build.gradle app id
 */


/*****
 * BUGS
 * ads manual calling has errors if done too often...
 * need to hide borders on any ad, then re-show upon close
 * play/pause notif stopped working api 16 - had to force close. Sometimes it just randomly disappears
 * add roboto font app-wide
 *
 * TODO:
 * analytics
 * extensively test onpause/resume/destroy of all fragments for worldservice, ondestroy of imageedit def not working
 * check hiding/showing things on notifications
 *
 *
 * MAYBE TODO:
 * need to detect keyboard open ----------------------- can't do without much hacking on accessibility
 * First timer tutorial
 * gradient option for galaxy edge
 * small notification play/pause: http://www.androidbegin.com/tutorial/android-custom-notification-tutorial/
 * figure out image sizing for tablets
 * need to make sure thickness is set proportionally
 * add option to only show notification when borders on
 * fullscreen may be more accurate with another invisible overlay and detecting the topleft position: http://stackoverflow.com/questions/18049543/is-it-possible-to-detect-when-any-application-is-in-full-screen-in-android
 *
 */

/** adb install -i store_package_to_test /path/to/apk **/


public class MainActivity extends Activity
    implements
        EditFragment.EditFragmentListener,
        EditFragmentTransition.EditFragmentTransitionListener,
        HomeFragment.HomeFragmentListener,
        WelcomeFragment.WelcomeFragmentListener,
        ImageEditFragment.ImageEditFragmentListener,
        ThemeListFragment.EditPrefListFragmentListener,
        SettingsPrefFragment.SettingsPrefFragmentListener,
        CustomFragment.CustomFragmentListener,
        TJConnectListener, TJPlacementListener
{

    private WelcomeFragment fragmentWelcome;
    private HomeFragment fragmentHome;
    private EditFragment fragmentEdit;
    private CustomFragment fragmentCustom;
    private SharedPreferences preferences;
    private SettingsPrefFragment fragmentSettings;
    private EditFragmentTransition fragmentEditTransition;
    private ImageEditFragment currImageEditFragment = null;

    //final private int REQUEST_STARTUP_PERMISSION = 1;
    //final private int REQUEST_FILE_PERMISSION = 2;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 3;
    //final private int REQUEST_SHARE_FILE_PERMISSION = 4;
    final private int REQUEST_OVERLAY_PERMISSION = 5;

    private String lastSuccessfulSessionImport = "";

    private String TAG = "";

    /************************************* tapjoy (ads) ******************************************/

    private String TAPJOY_KEY = "";
    TJPlacementListener tjPlacementListener = this;
    TJPlacement tjNews, tjInterstitial, tjVideo, tjOfferwall;

    private void initTapJoy(){
        try {
            TAPJOY_KEY = getString(R.string.tapjoy_key);
            Tapjoy.connect(this.getApplicationContext(), TAPJOY_KEY, null, this);
            Tapjoy.setDebugEnabled(true); /////////////////////////////////////////////////////////////// remove this before prod release
            Tapjoy.setGcmSender("143564304788"); //project id from google developer console, also requires server api key added to tapjoy dash

            //all placements should init here
            tjNews = new TJPlacement(this, getString(R.string.tapjoy_news_placement), tjPlacementListener);
            tjInterstitial = new TJPlacement(this, getString(R.string.tapjoy_interstitial_placement), tjPlacementListener);
            tjVideo = new TJPlacement(this, getString(R.string.tapjoy_video_placement), tjPlacementListener);
            tjOfferwall = new TJPlacement(this, getString(R.string.tapjoy_offerwall_placement), tjPlacementListener);
        } catch (Exception e) {
            Log.e("error", "cannot load ads: " + e.getMessage());
        }
    }

    public int getTJPoints(){
        return preferences.getInt(getString(R.string.b_points),0);
    }

    public void requestTJCurrentPoints(){
        if(Tapjoy.isConnected() && !preferences.getBoolean(getString(R.string.unlocked_pref),false)) {
            Tapjoy.getCurrencyBalance(new TJGetCurrencyBalanceListener() {
                @Override
                public void onGetCurrencyBalanceResponse(String currencyName, int balance) {
                    Log.d("points",balance+"");
                    preferences.edit().putInt(getString(R.string.b_points),balance).commit();
                }

                @Override
                public void onGetCurrencyBalanceResponseFailure(String error) {
                    Log.i("Tapjoy", "getCurrencyBalance error: " + error);
                }
            });
        }
    }

    public void showInvoluntaryAd(){
        requestTJCurrentPoints();

        int launchedCount = preferences.getInt(getString(R.string.edit_fragment_show_count),0);
        Log.d(TAG,launchedCount+"");
        launchedCount++;
        preferences.edit().putInt(getString(R.string.edit_fragment_show_count),launchedCount).commit();
        if(launchedCount % 4 == 0 && !preferences.getBoolean(getString(R.string.unlocked_pref),false)) {
            //////////want to stop service here maybe, then start after ad ////////////////////////////////////////////////////

            int adNum = preferences.getInt(getString(R.string.ad_rotation),1);
            switch (adNum) {
                case 1: showInterstitial(); break;
                case 2: showInterstitial(); break;
                case 3: showOfferwall(); break;
                case 4: showInterstitial(); break;
                case 5: showVideo(); break;
            }
            adNum++;
            if(adNum == 6) adNum = 1;
            preferences.edit().putInt(getString(R.string.ad_rotation),adNum).commit();
        }
    }

    public void showVoluntaryAd(){
        showVideo();
    }

    public void showInterstitial(){
        if(tjInterstitial == null){
            tjInterstitial = new TJPlacement(this, getString(R.string.tapjoy_interstitial_placement), tjPlacementListener);
            tjInterstitial.requestContent();
            tjInterstitial.showContent();
        }
        else
        if(tjInterstitial.isContentReady() || tjInterstitial.isContentAvailable()) {
            Log.d(TAG,"interstitial go");
            tjInterstitial.showContent();
        } else {
            Log.d(TAG,"interstitial fail");
        }
    }

    public void showVideo(){
        if(tjVideo == null) {
            tjVideo = new TJPlacement(this, getString(R.string.tapjoy_video_placement), tjPlacementListener);
            tjVideo.requestContent();
            tjVideo.showContent();
        }
        else
        if(tjVideo.isContentReady() || tjVideo.isContentAvailable()) {
            Log.d(TAG,"tjVideo go");
            tjVideo.showContent();
        } else {
            Log.d(TAG,"tjVideo fail");
        }
    }

    public void showOfferwall(){
        if(tjOfferwall == null){
            tjOfferwall = new TJPlacement(this, getString(R.string.tapjoy_offerwall_placement), tjPlacementListener);
            tjOfferwall.requestContent();
            tjOfferwall.showContent();
        }
        else {
            //if(tjOfferwall.isContentReady() || tjOfferwall.isContentAvailable()) {
            Log.d(TAG, "tjOfferwall go");
            tjOfferwall.requestContent();
            tjOfferwall.showContent();
            //} else {
            //    Log.d(TAG,"tjOfferwall fail");
            //}
        }
    }

    // called when Tapjoy connect call succeed
    @Override
    public void onConnectSuccess() {
        Log.d(TAG, "Tapjoy connect Succeeded");
        Log.d(TAG, "Tapjoy unlocked - " + preferences.getBoolean(getString(R.string.unlocked_pref),false));
        if(!preferences.getBoolean(getString(R.string.unlocked_pref),false)) {
            Log.d(TAG,"tapjoy actually connected - "+Tapjoy.isConnected());
            if(Tapjoy.isConnected()) {
                tjNews.requestContent();
                tjInterstitial.requestContent();
                tjVideo.requestContent();
                tjOfferwall.requestContent();
            } else {
                Log.d("%s", "Tapjoy SDK must finish connecting before requesting content.");
            }
        }
    }

    // called when Tapjoy connect call failed
    @Override
    public void onConnectFailure() {
        Log.d(TAG, "Tapjoy connect Failed");
    }

    @Override
    public void onRequestSuccess(TJPlacement tjPlacement) {
        requestTJCurrentPoints();
        if(tjPlacement.equals(tjNews)) {
            if(tjNews.isContentReady() || tjNews.isContentAvailable()) {
                Log.d(TAG,"appstart go");
                tjNews.showContent();
            } else {
                Log.d(TAG,"appstart fail");
            }
        }
    }

    @Override
    public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {

    }

    @Override
    public void onContentReady(TJPlacement tjPlacement) {

    }

    @Override
    public void onContentShow(TJPlacement tjPlacement) {
        stopWorldService();
    }

    @Override
    public void onContentDismiss(TJPlacement tjPlacement) {
        boolean editMode = preferences.getBoolean(getString(R.string.service_editmode),false);
        String editPos = preferences.getString(getString(R.string.service_editpos),"");
        startWorldService(editMode,editPos);
    }

    @Override
    public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {

    }

    @Override
    public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {

    }

    /******************************* billing functions ***************************************/

    public static boolean isPaidVersion(Context context){
        return context.getString(R.string.pref_namespace).equals(context.getString(R.string.paid_app_name))
                && context.getPackageName().equals(context.getString(R.string.paid_app_name));
    }

    public void adUnlock() {
        preferences.edit().putBoolean(getString(R.string.unlocked_pref),true).commit();
        if(fragmentHome != null && fragmentHome.isVisible()) {
            fragmentHome.hidePointsButton();
        }
        hideServiceNotification();
        showServiceNotification();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.ads_unlocked_title);
        alertDialogBuilder
                .setMessage(R.string.ads_unlocked_message)
                .setPositiveButton(R.string.ads_unlocked_go, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /******************************* utility functions ****************************************/

    private void shareImage(Uri uri, String extension, String message) {
        PackageManager pm = getPackageManager();
        String installer = pm.getInstallerPackageName(getApplicationContext().getPackageName());

        if(message.equals(getString(R.string.share_image_theme_flag))) {
            message = getString(R.string.share_theme_gp_free);
            if (getPackageName().equals(getString(R.string.paid_app_name)))
                message = getString(R.string.share_theme_gp_paid);
            if (installer != null && !installer.equals("") && installer.contains(getString(R.string.installer_amazon))) {
                message = getString(R.string.share_theme_azn_free);
                if (getPackageName().equals(getString(R.string.paid_app_name)))
                    message = getString(R.string.share_theme_azn_paid);
            }
        }

        try {
            if(extension.equals(getString(R.string.image_type_jpg))) extension = getString(R.string.image_type_jpeg);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(getString(R.string.image_mimetype_start)+extension);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(intent, getString(R.string.share_image_chooser_message)));
        } catch (Exception e) {
            longToast(getString(R.string.share_image_error_message));
        }
    }

    void longToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        if (currImageEditFragment != null) {
            hideImageEditScreen();
        }
        else if (fragmentCustom != null && fragmentCustom.isVisible()) {
            fragmentCustom.onBackPressed();
        }
        else if (fragmentEdit != null && fragmentEdit.isVisible()) { // and then you define a method allowBackPressed with the logic to allow back pressed or not
            hideEditScreen();
        }
        else {
            super.onBackPressed();
        }
    }

    public void shareApp() {
        PackageManager pm = getPackageManager();
        String installer = pm.getInstallerPackageName(getApplicationContext().getPackageName());

        String appLink = getString(R.string.google_play_url) + getString(R.string.pref_namespace);
        if(installer != null && !installer.equals("") && installer.contains(getString(R.string.installer_amazon))) appLink = getString(R.string.amazon_url) + getString(R.string.pref_namespace);
        String sAux = getString(R.string.share_app_message_prefix) + appLink + " \n\n";

        try {
            //create Borders dir
            File bordersDir = Environment.getExternalStorageDirectory();
            File temp = new File(bordersDir.getAbsolutePath() + "/" + getString(R.string.borders_folder));
            if (!temp.exists() || !temp.isDirectory())
                temp.mkdir();
            bordersDir = temp;

            //add borders image to borders dir
            File imageFile = new File(bordersDir.getAbsolutePath() + "/" + getString(R.string.logo_filename));
            if (!imageFile.exists()) {
                ThemeJsonObject.copyAsset(this, bordersDir.getAbsolutePath(), getString(R.string.logo_filename));
            }

            shareImage(Uri.fromFile(imageFile), getString(R.string.image_type_png), sAux);
        } catch (IOException e) {
            longToast(getString(R.string.themes_directory_error));
        }
    }

    public void onShowSharePopup() {
        if(appPermissions(false)){
            fragmentHome.showSharePopup();
        }
    }

    private FragmentManager.OnBackStackChangedListener backStackChangedListener() {
        return new FragmentManager.OnBackStackChangedListener()
        {
            public void onBackStackChanged()
            {
                if (fragmentEdit != null && fragmentEdit.isVisible())
                {
                    fragmentEdit.customOnResume();
                }
            }
        };
    }

    public static String getFilePathFromUri(Context c, Uri uri) {
        String filePath = null;
        if ("content".equals(uri.getScheme())) {
            String[] filePathColumn = { MediaStore.MediaColumns.DATA };
            ContentResolver contentResolver = c.getContentResolver();

            Cursor cursor = contentResolver.query(uri, filePathColumn, null,
                    null, null);

            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        } else if ("file".equals(uri.getScheme())) {
            filePath = new File(uri.getPath()).getAbsolutePath();
        }
        return filePath;
    }

    private void handleFileIntent(Intent intent) {
        final String path = getFilePathFromUri(getApplicationContext(),intent.getData());
        final File imageUri = new File(path);
        Log.d("intent", "file intent: "+imageUri);
        if (!path.equals(lastSuccessfulSessionImport)) {
            longToast(getString(R.string.importing_toast));

            Point displaySize = new Point();
            Rect windowSize = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(windowSize);

            if(Build.VERSION.SDK_INT >= 17) {
                getWindowManager().getDefaultDisplay().getRealSize(displaySize);
            } else {
                displaySize.x = windowSize.width();
                displaySize.y = windowSize.height();
            }

            int width = displaySize.x - Math.abs(windowSize.width());
            int height = displaySize.y - Math.abs(windowSize.height());

            showEditScreen(width/2, height/2);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragmentEdit.updateThemeList(1,getString(R.string.theme_custom_title));
                    if(fragmentEdit.importTheme(imageUri)){
                        lastSuccessfulSessionImport = path;
                    }
                }
            }, 2250);
        } else {
            longToast(getString(R.string.locked_file_message));
        }
    }

    private void handleUrlIntent(Intent intent) {
        longToast(getString(R.string.unimplemented_feature_message));
        FeedbackUtils.askForFeedback(getApplicationContext());
    }

    public void showServiceNotification(){
        showServiceNotification(false);
    }

    public void showServiceNotification(boolean forceShow) {
        /**
         * This opens up a service that then builds a separate notification.
         * In order to stop the notification, the service must be updated,
         * thus telling it to update the notification separately.
         */
        EditFragment editFragment = (EditFragment)getFragmentManager().findFragmentByTag(getString(R.string.fragment_tag_edit));
        CustomFragment customFragment = (CustomFragment)getFragmentManager().findFragmentByTag(getString(R.string.fragment_tag_custom));

        if(appPermissions(false)
                && (forceShow ||
                   !preferences.getBoolean(getString(R.string.edit_mode_pref),false)
                && !preferences.getBoolean(getString(R.string.image_edit_mode_pref),false)
                && currImageEditFragment == null
                && (editFragment == null || !editFragment.isVisible())
                && (customFragment == null || !customFragment.isVisible()))
            ) {
            Log.d("service","show notif");
            Intent service = new Intent(this, NotificationService.class);
            startService(service);
        } else {
            hideServiceNotification();
        }
    }
    public void hideServiceNotification() {
        Log.d("service","hide notif");
        Intent service = new Intent(this, NotificationService.class);
        service.putExtra(getString(R.string.stop_notif_extra),true);
        startService(service);
    }

    private void showUnlockNotification() {
        Log.d("notify","show rating");

        Intent unlock = new Intent(this, MainActivity.class);
        unlock.putExtra(getString(R.string.unlockAppExtra), true);
        PendingIntent unlockApp = PendingIntent.getActivity(this, 0, unlock, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent unlockLater = new Intent(this, MainActivity.class);
        unlock.putExtra(getString(R.string.unlockAppLaterExtra), true);
        PendingIntent unlockAppLater = PendingIntent.getActivity(this, 0, unlockLater, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent unlockNever = new Intent(this, MainActivity.class);
        unlock.putExtra(getString(R.string.unlockAppNeverExtra), true);
        PendingIntent unlockAppNever = PendingIntent.getActivity(this, 0, unlockNever, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder n = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.unlock_notification_title))
                .setContentText(getString(R.string.unlock_notification_message))
                .setSmallIcon(R.drawable.app_notif_icon_unlock)
                .setContentIntent(unlockApp)
                .setAutoCancel(true)
                .setOngoing(false)
                .addAction(0, getString(R.string.unlock_notification_unlock), unlockApp)
                .addAction(0, getString(R.string.unlock_notification_later), unlockAppLater)
                .addAction(0, getString(R.string.unlock_notification_never), unlockAppNever);
        notificationManager.notify(R.integer.unlock_notification_id, n.build());
    }

    private void showRatingNotification() {
        Log.d("notify","show rating");

        Intent showStore = new Intent(this, FeedbackUtils.class);
        showStore.setAction(getString(R.string.rating_action_show));
        PendingIntent storeIntent = PendingIntent.getService(this, 0, showStore, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent later = new Intent(this, FeedbackUtils.class);
        later.setAction(getString(R.string.rating_action_later));
        PendingIntent laterIntent = PendingIntent.getService(this, 0, later, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent never = new Intent(this, FeedbackUtils.class);
        never.setAction(getString(R.string.rating_action_never));
        PendingIntent neverIntent = PendingIntent.getService(this, 0, never, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder n = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.rating_notification_title))
                .setContentText(getString(R.string.rating_notification_message))
                .setSmallIcon(R.drawable.app_notif_icon_rate)
                .setAutoCancel(true)
                .setOngoing(false)
                .setContentIntent(storeIntent)
                .addAction(0, getString(R.string.rating_notification_rate), storeIntent)
                .addAction(0, getString(R.string.rating_notification_later), laterIntent)
                .addAction(0, getString(R.string.rating_notification_never), neverIntent);
        notificationManager.notify(R.integer.rating_notification_id, n.build());
    }

    public static String getInstallerName(Context context){
        PackageManager pm = context.getPackageManager();
        String installer = pm.getInstallerPackageName(context.getApplicationContext().getPackageName());
        if(installer == null) {
            List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
            for (PackageInfo p : installedPackages) {
                if (p.packageName.contains(context.getString(R.string.samsung_package_contains))) {
                    installer = context.getString(R.string.installer_samsung);
                    break;
                }
            }
        }

        if(installer == null) installer = "";
        installer = installer.toLowerCase();

        if(installer.contains(context.getString(R.string.installer_google)) || installer.contains(context.getString(R.string.installer_android)))
            return context.getString(R.string.installer_google);
        if(installer.contains(context.getString(R.string.installer_amazon)))
            return context.getString(R.string.installer_amazon);
        if(installer.contains(context.getString(R.string.installer_samsung)))
            return context.getString(R.string.installer_samsung);
        return "";
    }

    public static void storeJump(Context context, String packageName){
        Log.d("store","storeJump");
        final String DEVELOPER_NAME = context.getString(R.string.developer_name);

        final String BB_DEVELOPER_ID = context.getString(R.string.dummy_text);
        final String NOOK_EAN = context.getString(R.string.dummy_text);
        final String BBID = context.getString(R.string.dummy_text);

        int AMAZON = AMMConstants.MARKET_SELECTOR_AMAZON;
        int SAMSUNG = AMMConstants.MARKET_SELECTOR_SAMSUNG;
        int GOOGLE = AMMConstants.MARKET_SELECTOR_GOOGLE;

        int selectedItem = -1;
        String installer = getInstallerName(context);
        if(installer.equals(context.getString(R.string.installer_amazon))) selectedItem = AMAZON;
        if(installer.equals(context.getString(R.string.installer_google))) selectedItem = GOOGLE;
        if(installer.equals(context.getString(R.string.installer_samsung))) selectedItem = SAMSUNG;

        AMMLinks.marketShowApp(context,
                selectedItem,
                packageName,
                NOOK_EAN,
                BBID,
                BB_DEVELOPER_ID,
                DEVELOPER_NAME);
    }

    /******************************** service functions *************************************/

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
            Log.d("service","starting service: "+Boolean.toString(editMode)+" "+editPos);
            preferences.edit()
                .putBoolean(getString(R.string.service_enabled_pref),true)
                .putBoolean(getString(R.string.service_editmode),editMode)
                .putString(getString(R.string.service_editpos),editPos)
            .commit();
            Intent worldService = new Intent(getApplicationContext(), WorldService.class);
            worldService.putExtra(getString(R.string.editModeExtra),editMode);
            worldService.putExtra(getString(R.string.editPosExtra),editPos);
            startService(worldService);
        }
        if(!editMode)
            showServiceNotification();
        else
            hideServiceNotification();
    }

    public void stopWorldService() {
        Log.d("service","stopping service");
        if(appPermissions(false)) {
            if (isServiceRunning() || preferences.getBoolean(getString(R.string.service_enabled_pref),false)) {
                preferences.edit()
                    .putBoolean(getString(R.string.service_enabled_pref),false)
                    .putBoolean(getString(R.string.service_editmode),false)
                    .putString(getString(R.string.service_editpos),"")
                .commit();
                stopService(new Intent(getApplicationContext(), WorldService.class));
            }
            showServiceNotification();
        }
    }

    public void onScreenshotReady() {
        /** this is called when the service is ready for a screenshot **/
        Log.d("testing", "onScreenshotReady");
        //setup directory
        File screenshotDir = Environment.getExternalStorageDirectory();
        File temp = new File(screenshotDir.getAbsolutePath() + "/" + getString(R.string.borders_folder));
        if(!temp.exists() || !temp.isDirectory())
            temp.mkdir();
        File temp2 = new File(temp + "/" + getString(R.string.screenshots_folder));
        if(!temp2.exists() || !temp2.isDirectory())
            temp2.mkdir();
        screenshotDir = temp2;

        //setup default view
        View v = getWindow().getDecorView().getRootView();
        if(WorldService.runningInstance != null)
            v = WorldService.runningInstance.serviceView.getRootView();

        //setup vars and file name
        v.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_hh-mm-ss");
        String date = simpleDateFormat.format(new Date());
        OutputStream out = null;
        File imageFile = new File(screenshotDir.getAbsolutePath() + "/" + getString(R.string.screenshot_prefix) + date + getString(R.string.screenshot_extension));

        //create image
        try {
            out = new FileOutputStream(imageFile);
            // choose JPEG format
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        } catch (Exception e) {
            longToast(getString(R.string.screenshot_error_message));
            Log.d("screenshot",e.getMessage());
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception exc) {
                longToast(getString(R.string.screenshot_error_message));
                Log.d("screenshot",exc.getMessage());
            }
        }

        stopScreenshotWorldService();

        //share new image
        shareImage(Uri.fromFile(imageFile),getString(R.string.image_type_png),getString(R.string.share_image_theme_flag));
    }

    public void startScreenshotWorldService() {
        Log.d("service","start screenshot");
        stopWorldService();

        Intent worldService = new Intent(getApplicationContext(), WorldService.class);
        worldService.putExtra(getString(R.string.screenshotExtra),true);
        startService(worldService);
    }

    public void stopScreenshotWorldService() {
        Log.d("service","stop screenshot");
        stopWorldService();
        if(preferences.getBoolean(getString(R.string.service_toggled_pref),false))
            startWorldService(false, "");
    }

    /********************************** edit and custom screen functions *************************************/

    @TargetApi(21)
    public void showEditScreen(float x, float y) {
        requestTJCurrentPoints();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragmentEdit.setAnimationXY(x, y);
            fragmentEditTransition.setAnimationXY(x, y);
            getFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, fragmentEditTransition, getString(R.string.fragment_tag_edit_transition))
                .addToBackStack(null)
            .commit();
            getFragmentManager().executePendingTransactions();
        } else {
            editTransitionOpenDone();
        }
    }

    public void editTransitionOpenDone(){
        //set main activity background
        findViewById(R.id.mainFrame).setBackgroundColor(getResources().getColor(R.color.color_accent_light));
        if(fragmentEdit.isAdded()) {
            getFragmentManager().beginTransaction().show(fragmentEdit).commit();
            getFragmentManager().executePendingTransactions();
        }
        else
            getFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, fragmentEdit, getString(R.string.fragment_tag_edit))
                .addToBackStack(null)
            .commit();
        getFragmentManager().executePendingTransactions();
    }

    @TargetApi(21)
    public void hideEditScreen() {
        requestTJCurrentPoints();

        Log.d("service","hideeditservice");
        stopWorldService();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.mainFrame).setBackgroundColor(getResources().getColor(R.color.color_accent_light));
            final float animationDuration = 500;
            final Animator unreveal = fragmentEdit.prepareUnrevealAnimator();

            unreveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    fragmentEditTransition.skipCreateAnimation = true;
                    getFragmentManager().popBackStack();
                    getFragmentManager().executePendingTransactions();
                    fragmentEditTransition.skipCreateAnimation = false;

                    findViewById(R.id.mainFrame).setBackgroundColor(getResources().getColor(R.color.primary));
                    final Animator unreveal2 = fragmentEditTransition.prepareUnrevealAnimator();

                    unreveal2.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // remove the fragment only when the animation finishes
                            try {
                                getFragmentManager().popBackStack();
                            } catch (IllegalStateException e) {
                            }
                            //to prevent flashing the fragment before removing it, execute pending transactions inmediately
                            getFragmentManager().executePendingTransactions();

                            showInvoluntaryAd();

                            if (appPermissions(false))
                                onFinishEdit();
                            else
                                showRationalDialog();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                    unreveal2.setInterpolator(new DecelerateInterpolator(2f));
                    unreveal2.setDuration((long)animationDuration);
                    unreveal2.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            unreveal.setInterpolator(new DecelerateInterpolator(2f));
            unreveal.setDuration((long)animationDuration);
            unreveal.start();
        } else {
            getFragmentManager().popBackStack();
            //to prevent flashing the fragment before removing it, execute pending transactions inmediately
            getFragmentManager().executePendingTransactions();

            showInvoluntaryAd();

            if (appPermissions(false))
                onFinishEdit();
            else
                showRationalDialog();
        }
    }

    @Override
    public void onFinishEdit() {
        requestTJCurrentPoints();

        if(preferences.getBoolean(getString(R.string.edit_mode_pref),false)) {
            Log.d("service", "onfinishedit");
            stopWorldService();
            preferences.edit().putBoolean(getString(R.string.edit_mode_pref), false).commit();
            if (preferences.getBoolean(getString(R.string.service_toggled_pref),false))
                startWorldService(false, "");
            showServiceNotification();
        }
    }

    @Override
    public void onStartEdit() {
        requestTJCurrentPoints();

        Log.d("service", "onstartedit");
        stopWorldService();
        startWorldService(false,"");
        hideServiceNotification();

        if(!preferences.getBoolean(getString(R.string.edit_mode_pref),false)) {
            //reset main activity background
            findViewById(R.id.mainFrame).setBackgroundColor(getResources().getColor(R.color.primary));
            preferences.edit().putBoolean(getString(R.string.edit_mode_pref), true).commit();
        }
    }

    public void showCustomScreen(){
        if(preferences.getInt(getString(R.string.custom_id), 0) < 1) {
            longToast("No custom theme selected to edit.");
            return;
        }

        hideServiceNotification();

        getFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, fragmentCustom, getString(R.string.fragment_tag_custom))
                .addToBackStack(null)
                .commit();
        getFragmentManager().executePendingTransactions();
    }

    @Override
    public void onThemeSelectionChange() {
        if(appPermissions(false)) {
            setThemeChange(false);
        } else {
            setThemeChange(true);
        }
    }

    private void setThemeChange(boolean revert) {
        if(revert){
            preferences.edit()
                .putInt(getString(R.string.theme_id), preferences.getInt(getString(R.string.prev_theme_id), getResources().getInteger(R.integer.default_theme_id)))
                .putString(getString(R.string.theme_key), preferences.getString(getString(R.string.prev_theme_key), getString(R.string.default_theme_key)))
            .commit();
        }

        EditFragment editFragment = (EditFragment)getFragmentManager().findFragmentByTag(getString(R.string.fragment_tag_edit));
        CustomFragment customFragment = (CustomFragment)getFragmentManager().findFragmentByTag(getString(R.string.fragment_tag_custom));

        if(currImageEditFragment == null && editFragment != null && editFragment.isVisible() && (customFragment == null || !customFragment.isVisible())) {
            if(preferences.getInt(getString(R.string.theme_id),getResources().getInteger(R.integer.default_theme_id)) == 1)
                editFragment.showCustomCont();
            else
                editFragment.hideCustomCont();
            Log.d("service","setthemechange");
            stopWorldService();
            startWorldService(false, "");
            hideServiceNotification();
        }
    }

    /****************************************** image edit screen **********************************************/

    public void showImageEditScreen(String editPos) {
        currImageEditFragment = ImageEditFragment.newInstance(editPos);
        getFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, currImageEditFragment, getString(R.string.fragment_tag_image_edit))
                .addToBackStack(null)
                .commit();
        getFragmentManager().executePendingTransactions();

        //onStartImageEdit(editPos); //don't need, already in the onresume
        showInvoluntaryAd();
        onStartImageEdit(preferences.getString(getString(R.string.last_edit_pos_pref), ""));
        hideServiceNotification();
    }

    public void hideImageEditScreen() {
        ///////////////////////////////////////////////////////////////////////// are you sure dialog? decided not to do
        preferences.edit().putBoolean(getString(R.string.edit_mode_pref),true).commit();

        Log.d("service","hideimageeditscreen");
        stopWorldService();
        getFragmentManager().popBackStack();
        getFragmentManager().executePendingTransactions();
        currImageEditFragment = null;
        startWorldService(true, "");
        showInvoluntaryAd();
    }

    public void hideCustomScreen() {
        Log.d("service","hidecustomscreen");

        stopWorldService();
        getFragmentManager().popBackStack();
        getFragmentManager().executePendingTransactions();
        startWorldService(false,"");

        showInvoluntaryAd();
    }

    public void onStartCustomScreen(){
        hideServiceNotification();
        stopWorldService();
        startWorldService(true,"");
    }

    public void onCustomDestroy() {
        showServiceNotification();
        if(preferences.getBoolean(getString(R.string.service_enabled_pref),false)) {
            stopWorldService();
            startWorldService(false,"");
        }
    }

    @Override
    public void onFinishImageEditDestroy() {
        Log.d("service","onfinishimageeditdestroy");
        preferences.edit().putBoolean(getString(R.string.image_edit_mode_pref),false).commit();
        stopWorldService();
        if(!preferences.getBoolean(getString(R.string.service_enabled_pref),false))
            startWorldService(false, "");
        showServiceNotification();
    }

    @Override
    public void onStartImageEdit(String editPos) {
        preferences.edit().putBoolean(getString(R.string.image_edit_mode_pref),true).commit();
        preferences.edit().putBoolean(getString(R.string.edit_mode_pref),false).commit();
        Log.d("service","onstartimageedit");
        stopWorldService();
        startWorldService(true, editPos);
        hideServiceNotification();
    }

    /************************************** settings functions ****************************************/

    public void showSettings() {
        Log.d("stupid","settings show");
        FrameLayout fl = (FrameLayout)findViewById(R.id.mainFrame);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fl.getLayoutParams();
        params.setMargins(42,42,42,42);
        fl.setLayoutParams(params);

        Log.d("stupid","settings replace fragment");
        getFragmentManager().beginTransaction()
            .replace(R.id.mainFrame, fragmentSettings, getString(R.string.fragment_tag_settings))
            .addToBackStack(null)
        .commit();
        getFragmentManager().executePendingTransactions();
    }

    public void onFinishSettings() {
        FrameLayout fl = (FrameLayout)findViewById(R.id.mainFrame);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fl.getLayoutParams();
        params.setMargins(0,0,0,0);
        fl.setLayoutParams(params);

        showServiceNotification();
    }

    public void onStartupPrefChecked(){
        if(!appPermissions(false)) {
            fragmentSettings.setStartupPref(false);
        }
    }

    /*********************************** home fragment functions **************************************/

    public void showHomeScreenFromWelcome(){
        getFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, fragmentHome, getString(R.string.fragment_tag_home))
                .commit();
        getFragmentManager().executePendingTransactions();
        showRationalDialog();
        preferences.edit().putBoolean(getString(R.string.image_edit_mode_pref),false).commit();
    }

    @Override
    public void firstTimer() {
        if(appPermissions(false)) {
            preferences.edit()
                    .putBoolean(getString(R.string.first_time_pref), false)
                    .putBoolean(getString(R.string.service_toggled_pref), true)
                    .putInt(getString(R.string.theme_id), getResources().getInteger(R.integer.default_theme_id))
                    .putString(getString(R.string.theme_key), getString(R.string.default_theme_key))
                    .commit();
            Log.d("service", "firsttimer");
            if(fragmentHome != null)
                fragmentHome.setServiceToggle();
        }
    }

    /******************************** main activity and permission functions **************************************/

    private void showUninstallMessage(){
        if(!preferences.getBoolean(getString(R.string.unlocked_pref),false) || preferences.getBoolean(getString(R.string.dismiss_thank_you),false))
            return;

        String msg = getString(R.string.uninstall_popup_message);

        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.uninstall_popup_title));

        final String lookingFor = getString(R.string.free_app_name);
        boolean paidVersion = false;

        if(getString(R.string.pref_namespace).equals(getString(R.string.paid_app_name))) {
            //see if free version exists
            final PackageManager pm = getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo packageInfo : packages) {
                Log.d("blahblah",packageInfo.packageName+" "+Boolean.toString(packageInfo.packageName.equals(lookingFor)));

                if(packageInfo.packageName.equals(lookingFor)) {
                    msg += getString(R.string.uninstall_popup_extra_message);
                    paidVersion = true;
                    break;
                }
            }
        }

        alertDialogBuilder.setMessage(msg);

        if(paidVersion) {
            alertDialogBuilder.setPositiveButton(getString(R.string.uninstall_popup_uninstall_go), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    preferences.edit().putBoolean(getString(R.string.dismiss_thank_you),true).commit();
                    dialog.dismiss();
                    Uri packageURI = Uri.parse(getString(R.string.package_search_prefix)+lookingFor);
                    Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                    startActivity(uninstallIntent);
                }
            });
            alertDialogBuilder.setNegativeButton(getString(R.string.uninstall_popup_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        } else {
            alertDialogBuilder.setPositiveButton(getString(R.string.uninstall_popup_default_go), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    preferences.edit().putBoolean(getString(R.string.dismiss_thank_you),true).commit();
                    dialog.dismiss();
                }
            });
        }

        // create alert dialog
        android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        Log.d(TAG, "startActivityForResult() intent: " + intent + " requestCode: " + requestCode);
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() requestCode: " + requestCode + " resultCode: " + resultCode + " data: " + data);

        if(requestCode == REQUEST_OVERLAY_PERMISSION) {
            if(!appPermissions(false))
                showRationalDialog();
            else if(fragmentHome != null) {
                Log.d("service","onactres setservicetoggle");
                fragmentHome.setServiceToggle();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //preferences.edit().putBoolean(getString(R.string.temp_perm_dialog),false).commit();

        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                boolean hasPerms = true;
                if (grantResults.length > 1) {
                    for(int i=0;i<permissions.length;++i)
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
                            hasPerms = false;
                }
                if(!hasPerms)
                    showRationalDialog();
                else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)){
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse(getString(R.string.package_search_prefix) + getPackageName()));
                    startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
                } else {
                    appInitFunctions();
                    firstTimer();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void showRationalDialog(){
        if(!appPermissions(false)) {
            hideServiceNotification();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getString(R.string.ads_permission_title));
            alertDialogBuilder
                    .setMessage(getString(R.string.ads_permission_msg))
                    .setPositiveButton(getString(R.string.ads_permission_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            appPermissions(true);
                        }
                    })
                    .setNegativeButton(getString(R.string.ads_permission_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                /*.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (!permDialogAccepted)
                            finish();
                    }
                })*/;

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    @TargetApi(23)
    public boolean appPermissions(boolean requestPerms) {
        //if(!preferences.getBoolean(getString(R.string.temp_perm_dialog),false)) {
          //  preferences.edit().putBoolean(getString(R.string.temp_perm_dialog),true).commit();

            String locationCPermission = Manifest.permission.ACCESS_FINE_LOCATION;
            String readPhoneStatePermission = Manifest.permission.READ_PHONE_STATE;
            String getAccountsPermission = Manifest.permission.GET_ACCOUNTS;
            String filePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

            int hasFilePermission = ContextCompat.checkSelfPermission(this, filePermission);
            int hasLocCPermission = ContextCompat.checkSelfPermission(this, locationCPermission);
            int hasRPSPermission = ContextCompat.checkSelfPermission(this, readPhoneStatePermission);
            int hasAccountsPermission = ContextCompat.checkSelfPermission(this, getAccountsPermission);

            List<String> permissions = new ArrayList<>();
            if (hasLocCPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(locationCPermission);
            }
            if (hasRPSPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(readPhoneStatePermission);
            }
            if (hasAccountsPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(getAccountsPermission);
            }
            if (hasFilePermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(filePermission);
            }
            if (!permissions.isEmpty() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))) {
                String[] params = permissions.toArray(new String[permissions.size()]);
                /*if (!skipPopup && (ActivityCompat.shouldShowRequestPermissionRationale(this, locationCPermission)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, readPhoneStatePermission)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, getAccountsPermission)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, filePermission)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, startupPermission))
                        ) {
                    preferences.edit().putBoolean(getString(R.string.temp_perm_dialog),false).commit();
                    showRationalDialog();
                } else {*/
                if(requestPerms) {
                    if (!permissions.isEmpty())
                        ActivityCompat.requestPermissions(this, params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                    else {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse(getString(R.string.package_search_prefix) + getPackageName()));
                        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
                    }
                }
                //}
                return false;
            } else {
                return true;
            }
        //}
        //return false;
    }

    /**public void onNotificationSettingChecked(){
        if(!isNotificationSettingEnabled()){
            stopWorldService();
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        }
    }

    public boolean isNotificationSettingEnabled(){
        String enabledNotificationListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        String packageName = getPackageName();

        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }*/

    public void onAccessibilitySettingChecked(){
        if(!isAccessibilityEnabled()){
            stopWorldService();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }

    public boolean isAccessibilityEnabled(){
        String LOGTAG = "accessibilityperm";
        int accessibilityEnabled = 0;
        final String ACCESSIBILITY_SERVICE_NAME = getString(R.string.pref_namespace)+"/"+getString(R.string.package_name)+".BordersAccessibilityService";

        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d(LOGTAG, "ACCESSIBILITY: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.d(LOGTAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled==1){
            Log.d(LOGTAG, "***ACCESSIBILIY IS ENABLED***: ");


            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Log.d(LOGTAG, "Setting: " + settingValue);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Log.d(LOGTAG, "Setting: " + accessibilityService);
                    if (accessibilityService.equalsIgnoreCase(ACCESSIBILITY_SERVICE_NAME)){
                        Log.d(LOGTAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }

            Log.d(LOGTAG, "***END***");
        }
        else{
            Log.d(LOGTAG, "***ACCESSIBILIY IS DISABLED***");
        }
        return false;
    }

    private void appInitFunctions() {
        if(!Tapjoy.isConnected())
            initTapJoy();

        showServiceNotification(true);

        //track how many times the app has started and store it
        int appStartCount = preferences.getInt(getString(R.string.app_start_pref),0);
        appStartCount++;
        Log.d("appStartCount",appStartCount+"");
        preferences.edit().putInt(getString(R.string.app_start_pref),appStartCount).commit();

        // amount of times we should show the "rate us" and "unlock" notifications
        int interval = getResources().getInteger(R.integer.popup_interval);
        //alternate the two popups as long as the user hasn't said "never show them"
        if(!preferences.getBoolean(getString(R.string.first_time_pref),false) && appStartCount > interval) {
            if (appStartCount % (interval * 2) == 0 && !(preferences.getBoolean(getString(R.string.never_rate_pref), false) || preferences.getBoolean(getString(R.string.rate_us_pref), false)))
                showRatingNotification();
            else if (appStartCount % interval == 0
                    && !preferences.getBoolean(getString(R.string.unlocked_pref), false)
                    && !preferences.getBoolean(getString(R.string.unlocked_no_notify), false))
                showUnlockNotification();
        }

        showUninstallMessage();

        doExtras();
    }

    private void doExtras() {
        //handle import intent filters
        Intent intent = getIntent();
        Uri data = intent.getData();
        String action = intent.getAction();
        String type = intent.getType();
        String scheme = intent.getScheme();

        /*if(appPermissions(false) && !isServiceRunning() && preferences.getBoolean(getString(R.string.service_toggled_pref),false))
            startWorldService(
                preferences.getBoolean(getString(R.string.service_editmode),false),
                preferences.getString(getString(R.string.service_editpos),"")
            );*/

        if (action != null && scheme != null) {
            Log.d("intent",data.getHost()+"  "+data.getPath()+"  "+data.getScheme()+"  "+type+"  "+action+"  "+data);

            if (scheme.toLowerCase().startsWith(getString(R.string.scheme_http))) {
                handleUrlIntent(intent);
            } else {
                handleFileIntent(intent);
            }
        }

        Boolean unlockExtra = intent.getBooleanExtra(getString(R.string.unlockAppExtra),false);
        Boolean goUnlockExtra = intent.getBooleanExtra(getString(R.string.unlockAppProcessExtra),false);
        Boolean unlockLater = intent.getBooleanExtra(getString(R.string.unlockAppLaterExtra),false);
        Boolean unlockNever = intent.getBooleanExtra(getString(R.string.unlockAppNeverExtra),false);

        if(unlockExtra) {
            UnlockDialog unlockDialog = new UnlockDialog(this,null);
            unlockDialog.showDialog();
        } else
        if(unlockLater) {
            //do nothing since we autocancel
        } else
        if(unlockNever) {
            preferences.edit().putBoolean(getString(R.string.unlocked_no_notify),true).commit();
        } else
        if(goUnlockExtra) {
            storeJump(this, getString(R.string.paid_app_name));
            ///////////////////////////////////////////////////////////////////////////////////////processUnlock();
        }
    }

    public void handleUncaughtException (Thread thread, final Throwable e) {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();

                e.printStackTrace();
                stopWorldService();
                if(preferences.getBoolean(getString(R.string.service_toggled_pref),false))
                    startWorldService(false,"");
                longToast(getString(R.string.app_crash_message));
                FeedbackUtils.askForFeedback(getApplicationContext());

                Looper.loop();
            }
        }.start();
        try
        {
            Thread.sleep(4000); // Let the Toast display before app will get shutdown
        }
        catch (InterruptedException e2) { e2.printStackTrace(); }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("blah","oncreate main");

        super.onCreate(savedInstanceState);

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                //handleUncaughtException (thread, e);
                e.printStackTrace();
                System.exit(2);
            }
        });

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        setContentView(R.layout.activity_main);

        TAG = getString(R.string.log_tag);

        //get fragment instances
        fragmentWelcome = new WelcomeFragment();
        fragmentHome = new HomeFragment();
        fragmentEdit = new EditFragment();
        fragmentEditTransition = new EditFragmentTransition();
        fragmentSettings = new SettingsPrefFragment();
        fragmentCustom = new CustomFragment();
        preferences = getSharedPreferences(getString(R.string.pref_namespace),MODE_PRIVATE);

        if(isPaidVersion(this))
            preferences.edit().putBoolean(getString(R.string.unlocked_pref),true).commit();

        getFragmentManager().addOnBackStackChangedListener(backStackChangedListener());

        //preferences.edit().clear().commit(); ///////////////////////////////////////////////////////////////////remove before release

        //start app with main home fragment
        Log.d("prefs","mainact ft: "+Boolean.toString(preferences.getBoolean(getString(R.string.first_time_pref),true)));
        if(preferences.getBoolean(getString(R.string.first_time_pref),true)) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.mainFrame, fragmentWelcome, getString(R.string.fragment_tag_welcome))
                    .commit();
            getFragmentManager().executePendingTransactions();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.mainFrame, fragmentHome, getString(R.string.fragment_tag_home))
                    .commit();
            getFragmentManager().executePendingTransactions();
            preferences.edit().putBoolean(getString(R.string.image_edit_mode_pref),false).commit();

            if(!appPermissions(false))
                showRationalDialog();
            else
                appInitFunctions();
        }
    }

    @Override
    protected void onResume() {
        requestTJCurrentPoints();

        //receiver to handle our special screenshot view
        LocalBroadcastManager.getInstance(this).registerReceiver(screenshotReceiver,
                new IntentFilter(getString(R.string.intent_filter_service_ready)));
        //receiver to handle our toggle button for the on/off notification
        LocalBroadcastManager.getInstance(this).registerReceiver(toggleReceiver,
                new IntentFilter(getString(R.string.intent_filter_toggle_switch)));

        if(preferences.getBoolean(getString(R.string.service_enabled_pref),false)) {
            stopWorldService();
            startWorldService(false,"");
        }

        showServiceNotification();

        super.onResume();
    }

    private BroadcastReceiver screenshotReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onScreenshotReady();
        }
    };
    private BroadcastReceiver toggleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean message = intent.getBooleanExtra(getString(R.string.toggleExtra),false);
            if(fragmentHome != null) {
                Log.d("service","notif setting toggle");
                fragmentHome.setToggleWithoutService(message);
            }
        }
    };

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(screenshotReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(toggleReceiver);

        showServiceNotification(true);

        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Tapjoy.onActivityStart(this);
    }

    @Override
    protected void onStop() {
        Tapjoy.onActivityStop(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
