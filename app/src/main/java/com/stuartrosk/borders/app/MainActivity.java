package com.stuartrosk.borders.app;

import android.Manifest;
import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.tapjoy.*;
import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.Purchase;

import java.util.ArrayList;
import java.util.List;

/*****
 *
 * TODO:
 * test landscape mode
 * Implement free/paid features:
 *   unlock app and signed code checking ----test
 *   notifications for unlocking ----test
 * material design -- test in emulator
 * images images images
 * various icons
 * figure out image sizing for tablets
 * analytics
 * need to detect keyboard open
 * detect system bar expanded and hide the service while they are
 * share theme
 * save multiple custom themes
 * export theme
 * import theme
 *
 * MAYBE TODO:
 * Add photo cropping and save to new folder
 * First timer tutorial
 * gradient option for galaxy edge
 *
 * BUGS:
 * ads keep jacking up fragments
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
        CustomListFragment.CustomListFragmentListener,
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

    final private int REQUEST_STARTUP_PERMISSION = 1;
    final private int REQUEST_FILE_PERMISSION = 2;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 3;
    final private int REQUEST_SHARE_FILE_PERMISSION = 4;
    final private int REQUEST_OVERLAY_PERMISSION = 5;

    final private String TAPJOY_KEY = "xlKCXThXS0i5KGsSUMX-uwECBweWa3E6RxmgvJtIK1vHJ2vds-VfRy-ifft0";
    TJPlacementListener tjPlacementListener = this;
    TJPlacement tjNews, tjInterstitial, tjVideo, tjOfferwall;

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

    /**billing vars**/
    // Debug tag for logging
        static final String TAG = "BordersBilling";
    // Does the user have the premium upgrade?
        boolean mIsPremium = false;
    // (arbitrary) request code for the purchase flow
        static final int RC_REQUEST = 10001;
    // The helper object
        OpenIabHelper mHelper;
    // billing setup done
        private Boolean setupDone;
    // alert function
    void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    public void showHomeScreenFromWelcome(){
        getFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, fragmentHome, "H")
                .commit();
        getFragmentManager().executePendingTransactions();
        showRationalDialog();
        preferences.edit().putBoolean(getString(R.string.image_edit_mode_pref),false).commit();
    }

    //function to run unlock purchase
    public void processUnlock(){
        Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");

        if (setupDone == null) {
            toast("Billing Setup is not completed yet, please try again in a minute.");
            return;
        }

        if (!setupDone) {
            toast("Billing Setup failed. :( Please try restarting the app and try again.");
            return;
        }

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        try {
            mHelper.launchPurchaseFlow(this, IAPConfig.SKU_PREMIUM, RC_REQUEST,
                    mPurchaseFinishedListener, payload);
        } catch (Exception e) {
            Log.d(TAG,"error: "+e.getMessage());
            toast("Failed to retrieve store listing.");
        }
    }
    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }
    // Listener that's called when we finish querying the items and subscriptions we own
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        Log.d(TAG, "Query inventory finished.");
        if (result.isFailure()) {
            checkUnlocked(false);
            toast("Failed to retrieve store listing.");
            Log.d(TAG, result.toString());
            return;
        }

        Log.d(TAG, "Query inventory was successful.");

        /*
         * Check for items we own. Notice that for each purchase, we check
         * the developer payload to see if it's correct! See
         * verifyDeveloperPayload().
         */

        // Do we have the premium upgrade?
        Purchase premiumPurchase = inventory.getPurchase(IAPConfig.SKU_PREMIUM);
        mIsPremium = premiumPurchase != null && verifyDeveloperPayload(premiumPurchase);
        Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

        checkUnlocked(false);
        Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
        Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
        if (result.isFailure()) {
            toast("Error purchasing. Please try again.");
            Log.d(TAG,result.toString());
            return;
        }
        if (!verifyDeveloperPayload(purchase)) {
            toast("Error purchasing. Authenticity verification failed.");
            return;
        }

        Log.d(TAG, "Purchase successful.");

        if (purchase.getSku().equals(IAPConfig.SKU_PREMIUM)) {
            // bought the premium upgrade!
            Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
            toast("Thank you for unlocking the app!! :D");
            mIsPremium = true;
            checkUnlocked(false);

            //track purchase with tapjoy
            Tapjoy.trackPurchase(IAPConfig.SKU_PREMIUM, IAPConfig.SKU_PREMIUM, "borders app sig", null);
        }
        }
    };
    /**end billing vars**/

    /** service functions **/

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
        showServiceNotification();
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

    public void startScreenshotWorldService() {
        Log.d("service","start screenshot");
        stopWorldService();
        if(!isServiceRunning()) {
            Intent worldService = new Intent(getApplicationContext(), WorldService.class);
            worldService.putExtra("screenshot",true);
            startService(worldService);
        }
    }

    public void stopScreenshotWorldService() {
        Log.d("service","stop screenshot");
        stopWorldService();
        if(preferences.getBoolean(getString(R.string.service_enabled_pref),false))
            startWorldService(false, "");
    }

    @Override
    public void onBackPressed() {
        if (currImageEditFragment != null) {
            hideImageEditScreen();
        }
        else if (fragmentEdit != null && fragmentEdit.isVisible()) { // and then you define a method allowBackPressed with the logic to allow back pressed or not
            hideEditScreen();
        }
        else if (fragmentCustom != null && fragmentCustom.isVisible()) {
            fragmentCustom.onBackPressed();
        }
        else {
            super.onBackPressed();
        }
    }

    /** edit screen functions **/

    @TargetApi(21)
    public void showEditScreen(float x, float y) {
        requestTJCurrentPoints();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragmentEdit.setAnimationXY(x, y);
            fragmentEditTransition.setAnimationXY(x, y);
            getFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, fragmentEditTransition, "T")
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
                .add(R.id.mainFrame, fragmentEdit, "E")
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

            showInvoluntaryAd();
        }
    }

    @Override
    public void onStartEdit() {
        requestTJCurrentPoints();

        if(!preferences.getBoolean(getString(R.string.edit_mode_pref),false)) {
            Log.d("service", "onstartedit");
            //reset main activity background
            findViewById(R.id.mainFrame).setBackgroundColor(getResources().getColor(R.color.primary));
            boolean editMode = false;
            if (preferences.getInt(getString(R.string.theme_id), 1) == 1)
                editMode = true;
            //stopWorldService();
            //startWorldService(editMode, "");
            preferences.edit().putBoolean(getString(R.string.edit_mode_pref), true).commit();
            showServiceNotification();
        }
    }

    public void showCustomScreen(){
        getFragmentManager().beginTransaction()
                .add(R.id.mainFrame, fragmentCustom, "C")
                .addToBackStack(null)
                .commit();
        getFragmentManager().executePendingTransactions();

        fragmentEdit.updatePreferenceList(1,"Custom"); //force theme to be "custom"
    }

    @Override
    public void onThemeSelectionChange() {
        if(appPermissions(false)) {
            setThemeChange(false);
        } else {
            setThemeChange(true);
        }
    }

    public void onCustomSelectionChange() {
        setThemeChange(false);
    }

    private void setThemeChange(boolean revert) {
        if(revert){
            preferences.edit()
                .putInt(getString(R.string.theme_id), preferences.getInt(getString(R.string.prev_theme_id), getResources().getInteger(R.integer.default_theme_id)))
                .putString(getString(R.string.theme_key), preferences.getString(getString(R.string.prev_theme_key), getString(R.string.default_theme_key)))
            .commit();
        }

        EditFragment editFragment = (EditFragment)getFragmentManager().findFragmentByTag("E");
        boolean editMode = false;

        if(editFragment != null && editFragment.isVisible()) {
            if(preferences.getInt(getString(R.string.theme_id),1) == 1)
                editFragment.showCustomCont();
            else
                editFragment.hideCustomCont();
            Log.d("service","setthemechange");
            stopWorldService();
            startWorldService(editMode, "");
        }
    }

    /** image edit screen **/

    public void showImageEditScreen(String editPos) {
        currImageEditFragment = ImageEditFragment.newInstance(editPos);
        getFragmentManager().beginTransaction()
            .add(R.id.mainFrame, currImageEditFragment, "IE")
            .addToBackStack(null)
        .commit();
        getFragmentManager().executePendingTransactions();

        fragmentEdit.updatePreferenceList(1,"Custom"); //force theme to be "custom"
        onStartImageEdit(editPos);
        showInvoluntaryAd();
    }

    public void showInvoluntaryAd(){
        requestTJCurrentPoints();

        int launchedCount = preferences.getInt(getString(R.string.edit_fragment_show_count),0);
        Log.d(TAG,launchedCount+"");
        launchedCount++;
        preferences.edit().putInt(getString(R.string.edit_fragment_show_count),launchedCount).commit();
        if(launchedCount % 6 == 0 && !preferences.getBoolean(getString(R.string.unlocked_pref),false)) {
            int adNum = preferences.getInt(getString(R.string.ad_rotation),1);
            switch (adNum) {
                case 1: showOfferwall(); break;
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
            tjInterstitial = new TJPlacement(this, "Interstitial", tjPlacementListener);
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
            tjVideo = new TJPlacement(this, "Video", tjPlacementListener);
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
            tjOfferwall = new TJPlacement(this, "Offerwall", tjPlacementListener);
            tjOfferwall.requestContent();
            tjOfferwall.showContent();
        }
        else
        if(tjOfferwall.isContentReady() || tjOfferwall.isContentAvailable()) {
            Log.d(TAG,"tjOfferwall go");
            tjOfferwall.showContent();
        } else {
            Log.d(TAG,"tjOfferwall fail");
        }
    }

    public void hideImageEditScreen() {
        ///////////////////////////////////////////////////////////////////////// are you sure dialog? decided not to do
        requestTJCurrentPoints();

        preferences.edit().putBoolean(getString(R.string.edit_mode_pref),true).commit();

        Log.d("service","hideimageeditscreen");
        if(appPermissions(false)) {
            getFragmentManager().popBackStack();
            getFragmentManager().executePendingTransactions();
            currImageEditFragment = null;
            stopWorldService();
            startWorldService(true, "");
            showInvoluntaryAd();
        } else {
            stopWorldService();
            getFragmentManager().popBackStack();
            getFragmentManager().executePendingTransactions();
            currImageEditFragment = null;
        }
    }

    @Override
    public void onFinishImageEditDestroy() {
        Log.d("service","onfinishimageeditdestroy");
        preferences.edit().putBoolean(getString(R.string.image_edit_mode_pref),false).commit();
        stopWorldService();
        if(!preferences.getBoolean(getString(R.string.service_enabled_pref),false))
            startWorldService(false, "");
    }

    @Override
    public void onStartImageEdit(String editPos) {
        preferences.edit().putBoolean(getString(R.string.image_edit_mode_pref),true).commit();
        preferences.edit().putBoolean(getString(R.string.edit_mode_pref),false).commit();
        Log.d("service","onstartimageedit");
        stopWorldService();
        startWorldService(true, editPos);
    }

    /** settings functions **/

    public void showSettings() {
        FrameLayout fl = (FrameLayout)findViewById(R.id.mainFrame);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fl.getLayoutParams();
        params.setMargins(42,42,42,42);
        fl.setLayoutParams(params);

        getFragmentManager().beginTransaction()
            .replace(R.id.mainFrame, fragmentSettings, "S")
            .addToBackStack(null)
        .commit();
        getFragmentManager().executePendingTransactions();
    }

    public void hideSettings() {
        getFragmentManager().popBackStack();
        getFragmentManager().executePendingTransactions();
        onFinishSettings();
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

    /** home fragment functions **/

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

    /**billing helper functions **/

    public void checkUnlocked(boolean forceLock){
        //if we don't force unlock and the store has returned that we've purchased, or we've already see the purchased pref
        if(!forceLock && (mIsPremium || preferences.getBoolean(getString(R.string.unlocked_pref),false))) {
            preferences.edit().putBoolean(getString(R.string.unlocked_pref),true).commit();
            Log.d("purchase","TEST unlock purchased working!");
            //change ui elements to be unlocked
            if(fragmentSettings != null)
                fragmentSettings.checkUnlockedPref();
            if(fragmentEdit != null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.mainFrame, fragmentEdit, "E")
                        .commit();
                getFragmentManager().executePendingTransactions();
                preferences.edit().putBoolean(getString(R.string.image_edit_mode_pref),true).commit();
            }
            if(fragmentHome != null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.mainFrame, fragmentHome, "H")
                        .commit();
                getFragmentManager().executePendingTransactions();
                preferences.edit().putBoolean(getString(R.string.image_edit_mode_pref),false).commit();
            }
        } else {
            //change ui elements to be locked
        }
    }

    /** main activity functions **/

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
        } else
        // Pass on the activity result to the helper for handling
        if (mHelper == null || !mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    private void initIAP(){
        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        //Only map SKUs for stores that using purchase with SKUs different from described in store console.
        IAPConfig.init();
        OpenIabHelper.Options.Builder builder = new OpenIabHelper.Options.Builder()
                .setStoreSearchStrategy(OpenIabHelper.Options.SEARCH_STRATEGY_INSTALLER_THEN_BEST_FIT)
                .setVerifyMode(OpenIabHelper.Options.VERIFY_EVERYTHING)
                .addStoreKeys(IAPConfig.STORE_KEYS_MAP)
                .addAvailableStoreNames(new String[] {
                        OpenIabHelper.NAME_GOOGLE,
                        OpenIabHelper.NAME_AMAZON,
                        OpenIabHelper.NAME_SAMSUNG
                });
        mHelper = new OpenIabHelper(this, builder.build());

        // enable debug logging (for a production application, you should set this to false).
        //mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    setupDone = false;
                    checkUnlocked(false);
                    toast("Problem setting up in-app billing: " + result);
                    return;
                }

                // Hooray, IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                setupDone = true;
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    private void initTapJoy(){
        try {
            Tapjoy.connect(this.getApplicationContext(), TAPJOY_KEY, null, this);
            Tapjoy.setDebugEnabled(true); /////////////////////////////////////////////////////////////// remove this before prod release
            Tapjoy.setGcmSender("143564304788"); //project id from google developer console, also requires server api key added to tapjoy dash

            //all placements should init here
            tjNews = new TJPlacement(this, "News", tjPlacementListener);
            tjInterstitial = new TJPlacement(this, "Interstitial", tjPlacementListener);
            tjVideo = new TJPlacement(this, "Video", tjPlacementListener);
            tjOfferwall = new TJPlacement(this, "Offerwall", tjPlacementListener);
        } catch (Exception e) {
            Log.e("error", "cannot load ads: " + e.getMessage());
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
                            Uri.parse("package:" + getPackageName()));
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

    public void onShowSharePopup() {
        if(appPermissions(false)){
            fragmentHome.showSharePopup();
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

            List<String> permissions = new ArrayList<String>();
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
                                Uri.parse("package:" + getPackageName()));
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

    private void appInitFunctions() {
        if(!Tapjoy.isConnected())
            initTapJoy();

        showServiceNotification();

        /**IAP stuff**/
        initIAP();
        /**end IAP**/

        //see if we need to enable unlocked interface features
        checkUnlocked(false);

        //track how many times the app has started and store it
        int appStartCount = preferences.getInt(getString(R.string.app_start_pref),0);
        appStartCount++;
        Log.d("appStartCount",appStartCount+"");
        preferences.edit().putInt(getString(R.string.app_start_pref),appStartCount).commit();

        // amount of times we should show the "rate us" and "unlock" notifications
        int interval = getResources().getInteger(R.integer.popup_interval);
        //alternate the two popups as long as the user hasn't said "never show them"
        if(!preferences.getBoolean(getString(R.string.first_time_pref),true)) {
            if (appStartCount % interval * 2 == 0 && !(preferences.getBoolean(getString(R.string.never_rate_pref), false) || preferences.getBoolean(getString(R.string.rate_us_pref), false)))
                showRatingNotification();
            else if (appStartCount % interval == 0
                    && !preferences.getBoolean(getString(R.string.unlocked_pref), false)
                    && !preferences.getBoolean(getString(R.string.unlocked_no_notify), false))
                showUnlockNotification();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        setContentView(R.layout.activity_main);

        //get fragment instances
        fragmentWelcome = new WelcomeFragment();
        fragmentHome = new HomeFragment();
        fragmentEdit = new EditFragment();
        fragmentEditTransition = new EditFragmentTransition();
        fragmentSettings = new SettingsPrefFragment();
        preferences = getSharedPreferences(getString(R.string.pref_namespace),MODE_PRIVATE);

        //preferences.edit().clear().commit(); ///////////////////////////////////////////////////////////////////remove before release

        //start app with main home fragment
        Log.d("prefs","mainact ft: "+Boolean.toString(preferences.getBoolean(getString(R.string.first_time_pref),true)));
        if(preferences.getBoolean(getString(R.string.first_time_pref),true)) {
            getFragmentManager().beginTransaction()
                    .add(R.id.mainFrame, fragmentWelcome, "W")
                    .commit();
            getFragmentManager().executePendingTransactions();
        } else {
            getFragmentManager().beginTransaction()
                    .add(R.id.mainFrame, fragmentHome, "H")
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

        super.onResume();
        //receiver to handle our special screenshot view
        LocalBroadcastManager.getInstance(this).registerReceiver(screenshotReceiver,
                new IntentFilter("service-ready"));
        //receiver to handle our toggle button for the on/off notification
        LocalBroadcastManager.getInstance(this).registerReceiver(toggleReceiver,
                new IntentFilter("toggle-switch"));

        Boolean unlockExtra = getIntent().getBooleanExtra("unlock_app",false);
        Boolean goUnlockExtra = getIntent().getBooleanExtra("process_unlock_app",false);
        Boolean unlockLater = getIntent().getBooleanExtra("unlock_app_later",false);
        Boolean unlockNever = getIntent().getBooleanExtra("unlock_app_never",false);
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
            processUnlock();
        }
    }

    private BroadcastReceiver screenshotReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fragmentHome.onScreenshotReady();
        }
    };
    private BroadcastReceiver toggleReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean message = intent.getBooleanExtra("toggle",false);
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
        super.onPause();
    }

    public void showServiceNotification() {
        Log.d("showNotification","here");
        if(appPermissions(false)) {
            Intent service = new Intent(this, NotificationService.class);
            startService(service);
        }
    }
    public void hideServiceNotification() {
        Intent service = new Intent(this, NotificationService.class);
        stopService(service);
    }

    private void showUnlockNotification() {
        Log.d("notify","show rating");

        Intent unlock = new Intent(this, MainActivity.class);
        unlock.putExtra("unlock_app", true);
        PendingIntent unlockApp = PendingIntent.getActivity(this, 0, unlock, 0);

        Intent unlockLater = new Intent(this, MainActivity.class);
        unlock.putExtra("unlock_app_later", true);
        PendingIntent unlockAppLater = PendingIntent.getActivity(this, 0, unlockLater, 0);

        Intent unlockNever = new Intent(this, MainActivity.class);
        unlock.putExtra("unlock_app_never", true);
        PendingIntent unlockAppNever = PendingIntent.getActivity(this, 0, unlockNever, 0);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder n = new NotificationCompat.Builder(this)
                .setContentTitle("Unlock Borders")
                .setContentText("Like Borders? Unlock to get all the features!")
                .setSmallIcon(R.drawable.app_notif_icon_unlock)
                .setContentIntent(unlockApp)
                .setAutoCancel(true)
                .setOngoing(false)
                .addAction(0, "Unlock!", unlockApp)
                .addAction(0, "Later...", unlockAppLater)
                .addAction(0, "Never :(", unlockAppNever);
        ;
        notificationManager.notify(R.integer.unlock_notification_id, n.build());
    }

    private void showRatingNotification() {
        Log.d("notify","show rating");

        Intent showStore = new Intent(this, FeedbackUtils.class);
        showStore.setAction("show_rating");
        PendingIntent storeIntent = PendingIntent.getService(this, 0, showStore, 0);

        Intent later = new Intent(this, FeedbackUtils.class);
        later.setAction("later_rating");
        PendingIntent laterIntent = PendingIntent.getService(this, 0, later, 0);

        Intent never = new Intent(this, FeedbackUtils.class);
        never.setAction("never_rating");
        PendingIntent neverIntent = PendingIntent.getService(this, 0, never, 0);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder n = new NotificationCompat.Builder(this)
                .setContentTitle("Rate Borders")
                .setContentText("Like Borders? Touch to rate it!")
                .setSmallIcon(R.drawable.app_notif_icon_rate)
                .setAutoCancel(true)
                .setOngoing(false)
                .setContentIntent(storeIntent)
                .addAction(0, "Rate Us!", storeIntent)
                .addAction(0, "Later...", laterIntent)
                .addAction(0, "Never :(", neverIntent);
        notificationManager.notify(R.integer.rating_notification_id, n.build());
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

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    /***** tapjoy override methods for listeners *****/
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
    }

    @Override
    public void onContentDismiss(TJPlacement tjPlacement) {

    }

    @Override
    public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {

    }

    @Override
    public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {

    }
    /***** end tapjoy methods *****/

    /*
    @Override
     protected void onDestroy() {
        //if we are supposed to keep service, restart without the edit controls
        if(preferences.getBoolean("starting_edit",false)) {
            preferences.edit().putBoolean("starting_edit",false).commit();
        } else {
            if (!preferences.getBoolean("serviceEnabled", false)) {
                stopService(new Intent(getApplicationContext(), WorldService.class));
            } else {
                Intent i = new Intent(getApplicationContext(), WorldService.class);
                stopService(i);
                startService(i);
            }
        }

        super.onDestroy();
    }
    @Override
    protected void onPause() {
        //if we are supposed to keep service, restart without the edit controls
        if(preferences.getBoolean("starting_edit",false)) {
            preferences.edit().putBoolean("starting_edit",false).commit();
        } else {
            if (!preferences.getBoolean("serviceEnabled", false)) {
                stopService(new Intent(getApplicationContext(), WorldService.class));
            } else {
                Intent i = new Intent(getApplicationContext(), WorldService.class);
                stopService(i);
                startService(i);
            }
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        //always just restart service with edit controls when resuming this screen
        Intent i = new Intent(getApplicationContext(), WorldService.class);
        stopService(i);
        i.putExtra("hideEditMode","true");
        startService(i);

        super.onResume();
    }
     */
}
