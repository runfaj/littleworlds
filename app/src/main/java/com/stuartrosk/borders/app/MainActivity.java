package com.stuartrosk.borders.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.tapjoy.*;
import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.Purchase;

/*****
 *
 * TODO:
 * Add app info to settings
 * test landscape mode
 * Implement free/paid features:
 *   locked custom areas ----test
 *   locked themes ----test
 *   unlock app and signed code checking ----test
 *   notifications for unlocking ----test
 *   ads in free version
 * material design
 * images images images
 * various icons
 * figure out image sizing for tablets
 *
 * MAYBE TODO:
 * Add photo cropping and save to new folder
 * First timer tutorial
 *
 */

/** adb install -i store_package_to_test /path/to/apk **/


public class MainActivity extends Activity
    implements EditFragment.EditFragmentListener,
        HomeFragment.HomeFragmentListener,
        ImageEditFragment.ImageEditFragmentListener,
        EditPrefListFragment.EditPrefListFragmentListener,
        SettingsPrefFragment.SettingsPrefFragmentListener,
        TJConnectListener, TJPlacementListener {

    private HomeFragment fragmentHome;
    private EditFragment fragmentEdit;
    private SharedPreferences preferences;
    private SettingsPrefFragment fragmentSettings;

    private static final String TAPJOY_KEY = "xlKCXThXS0i5KGsSUMX-uwECBweWa3E6RxmgvJtIK1vHJ2vds-VfRy-ifft0";
    TJPlacementListener tjPlacementListener = this;
    TJPlacement tjAppStart, tjEditView;

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

        mHelper.launchPurchaseFlow(this, IAPConfig.SKU_PREMIUM, RC_REQUEST,
                mPurchaseFinishedListener, payload);
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
        if(!isServiceRunning()) {
            Intent worldService = new Intent(getApplicationContext(), WorldService.class);
            worldService.putExtra("editMode",editMode);
            worldService.putExtra("editPos",editPos);
            startService(worldService);
        }
        showServiceNotification();
    }

    public void startScreenshotWorldService() {
        stopWorldService();
        if(!isServiceRunning()) {
            Intent worldService = new Intent(getApplicationContext(), WorldService.class);
            worldService.putExtra("screenshot",true);
            startService(worldService);
        }
    }

    public void stopScreenshotWorldService() {
        stopWorldService();
        if(preferences.getBoolean(getString(R.string.service_enabled_pref),false))
            startWorldService(false, "");
    }

    public void stopWorldService() {
        if(isServiceRunning())
            stopService(new Intent(getApplicationContext(), WorldService.class));
        showServiceNotification();
    }

    /** edit screen functions **/

    public void showEditScreen() {
        getFragmentManager().beginTransaction()
            .replace(R.id.mainFrame, fragmentEdit, "E")
            .addToBackStack(null)
        .commit();
        onStartEdit();
    }

    public void hideEditScreen() {
        getFragmentManager().popBackStack();
        onFinishEdit();
    }

    @Override
    public void onFinishEdit() {
        preferences.edit().putBoolean(getString(R.string.edit_mode_pref),false).commit();
        stopWorldService();
        if(preferences.getBoolean(getString(R.string.service_enabled_pref),false))
            startWorldService(false, "");
        showServiceNotification();
    }

    @Override
    public void onStartEdit() {
        boolean editMode = false;
        if(preferences.getInt(getString(R.string.theme_id),1) == 1)
            editMode = true;
        stopWorldService();
        startWorldService(editMode, "");
        preferences.edit().putBoolean(getString(R.string.edit_mode_pref),true).commit();
        showServiceNotification();
    }

    @Override
    public void onThemeSelectionChange() {
        boolean editMode = false;
        if(preferences.getInt(getString(R.string.theme_id),1) == 1)
            editMode = true;
        EditFragment testFragment = (EditFragment)getFragmentManager().findFragmentByTag("E");
        if(testFragment != null && testFragment.isVisible()) {
            stopWorldService();
            startWorldService(editMode, "");
        }
        if(fragmentEdit != null && fragmentEdit.isAdded() && fragmentEdit.isVisible()) {
            fragmentEdit.toggleEditIcons();
        }
    }

    /** image edit screen **/

    public void showImageEditScreen(String editPos) {
        getFragmentManager().beginTransaction()
            .replace(R.id.mainFrame, ImageEditFragment.newInstance(editPos), "IE")
            .addToBackStack(null)
        .commit();

        int launchedCount = preferences.getInt(getString(R.string.edit_fragment_show_count),0);
        launchedCount++;
        preferences.edit().putInt(getString(R.string.edit_fragment_show_count),launchedCount).commit();
        if(launchedCount % 4 == 0 && preferences.getBoolean(getString(R.string.unlocked_pref),false)) {
            if(tjEditView.isContentReady()) {
                tjEditView.showContent();
            }
        }

        fragmentEdit.updatePreferenceList(1); //force theme to be "custom"
        onStartImageEdit(editPos);
    }

    public void cancelImageEditScreen() {
        //////// are you sure dialog?
        hideImageEditScreen();
        onFinishImageEdit();
    }

    public void hideImageEditScreen() {
        getFragmentManager().popBackStack();
        stopWorldService();
        startWorldService(true, "");
    }

    @Override
    public void onFinishImageEdit() {
        preferences.edit().putBoolean(getString(R.string.edit_mode_pref),false).commit();
        stopWorldService();
        if(preferences.getBoolean(getString(R.string.service_enabled_pref),false))
            startWorldService(false, "");
    }

    @Override
    public void onStartImageEdit(String editPos) {
        preferences.edit().putBoolean(getString(R.string.edit_mode_pref),true).commit();
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
    }

    public void hideSettings() {
        getFragmentManager().popBackStack();
        onFinishSettings();
    }

    public void onFinishSettings() {
        FrameLayout fl = (FrameLayout)findViewById(R.id.mainFrame);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fl.getLayoutParams();
        params.setMargins(0,0,0,0);
        fl.setLayoutParams(params);

        showServiceNotification();
    }

    /** home fragment functions **/

    @Override
    public void firstTimer() {
        preferences.edit()
            .putBoolean(getString(R.string.first_time_pref), false)
            .putBoolean(getResources().getString(R.string.service_enabled_pref),true)
            .putInt(getString(R.string.theme_id), 2)
            .putString(getString(R.string.theme_key), "Cats")
        .commit();
        stopWorldService();
        startWorldService(false,"");
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
            if(fragmentEdit != null)
                getFragmentManager().beginTransaction()
                    .replace(R.id.mainFrame, fragmentEdit, "E")
                    .commit();
            if(fragmentHome != null)
                getFragmentManager().beginTransaction()
                    .replace(R.id.mainFrame, fragmentHome, "H")
                    .commit();

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

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
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
        fragmentHome = new HomeFragment();
        fragmentEdit = new EditFragment();
        fragmentSettings = new SettingsPrefFragment();

        //start app with main home fragment
        getFragmentManager().beginTransaction()
            .add(R.id.mainFrame, fragmentHome, "H")
        .commit();

        //reference to get app-wide preferences
        preferences = getSharedPreferences(getString(R.string.pref_namespace),MODE_PRIVATE);

        //start background service for persistent notification
        showServiceNotification();

        //init ads
        if(!preferences.getBoolean(getString(R.string.unlocked_pref),false)) {
            try {
                Tapjoy.connect(this.getApplicationContext(), TAPJOY_KEY, null, this);
                Tapjoy.setDebugEnabled(true); /////////////////////////////////////////////////////////////// remove this before prod release
                Tapjoy.setGcmSender("143564304788"); //project id from google developer console, also requires server api key added to tapjoy dash

                //all placements should init here
                tjAppStart = new TJPlacement(getApplicationContext(), "AppLaunch", tjPlacementListener);
                tjEditView = new TJPlacement(getApplicationContext(), "1/4 Edit Fragment Show", tjPlacementListener);
            } catch (Exception e) {
                Log.e("error", "cannot load ads: " + e.getMessage());
            }
        }

        /**IAP stuff**/
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
        if(appStartCount % interval*2 == 0 && !preferences.getBoolean(getString(R.string.never_rate_pref),false))
            showRatingNotification();
        else if (appStartCount % interval == 0
                 && !preferences.getBoolean(getString(R.string.unlocked_pref),false)
                 && !preferences.getBoolean(getString(R.string.unlocked_no_notify),false))
            showUnlockNotification();
    }

    @Override
    protected void onResume() {
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
        Intent service = new Intent(this, NotificationService.class);
        startService(service);
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
                .setSmallIcon(R.drawable.ic_play_arrow_white_48dp)
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
                .setSmallIcon(R.drawable.ic_play_arrow_white_48dp)
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
        if(!preferences.getBoolean(getString(R.string.unlocked_pref),false)) {
            if(Tapjoy.isConnected()) {
                tjAppStart.requestContent();
                tjEditView.requestContent();
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
        if(tjPlacement.equals(tjAppStart)) {
            if(tjAppStart.isContentReady()) {
                tjAppStart.showContent();
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
