package com.stuartrosk.borders;

import android.app.Service;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class WorldService extends Service {
    public class LocalBinder extends Binder {
        WorldService getService() {
            return WorldService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static WorldService runningInstance = null;

    private WindowManager windowManager;
    public RelativeLayout serviceView;
    private LayoutInflater inflater;
    private ImageView trcV, tlcV, brcV, blcV,
            tlmV, trmV, blmV, brmV,
            sltV, slmV, slbV,
            srtV, srmV, srbV;
    private ImageJsonObject trcO, tlcO, brcO, blcO,
            tlmO, trmO, blmO, brmO,
            sltO, slmO, slbO,
            srtO, srmO, srbO, preview;
    private float startWindowWidth, startWindowHeight;
    SharedPreferences preferences;
    //GestureLibrary gestureLibrary = null;
    //GestureOverlayView gestureOverlayView;

    private boolean editMode = false;
    private String editPos;

    private ThemeJsonObject.Theme currTheme;

    private void initVars() {
        RelativeLayout s = serviceView;

        // corners
        trcV = (ImageView)s.findViewById(R.id.topRightCorner);
        tlcV = (ImageView)s.findViewById(R.id.topLeftCorner);
        brcV = (ImageView)s.findViewById(R.id.bottomRightCorner);
        blcV = (ImageView)s.findViewById(R.id.bottomLeftCorner);
        // top and bottom
        brmV = (ImageView)s.findViewById(R.id.bottomRightMiddle);
        blmV = (ImageView)s.findViewById(R.id.bottomLeftMiddle);
        trmV = (ImageView)s.findViewById(R.id.topRightMiddle);
        tlmV = (ImageView)s.findViewById(R.id.topLeftMiddle);
        //left side
        slbV = (ImageView)s.findViewById(R.id.sideLeftBottom);
        slmV = (ImageView)s.findViewById(R.id.sideLeftMiddle);
        sltV = (ImageView)s.findViewById(R.id.sideLeftTop);
        //right side
        srbV = (ImageView)s.findViewById(R.id.sideRightBottom);
        srmV = (ImageView)s.findViewById(R.id.sideRightMiddle);
        srtV = (ImageView)s.findViewById(R.id.sideRightTop);

        trcO = new ImageJsonObject();
        tlcO = new ImageJsonObject();
        brcO = new ImageJsonObject();
        blcO = new ImageJsonObject();
        tlmO = new ImageJsonObject();
        trmO = new ImageJsonObject();
        blmO = new ImageJsonObject();
        brmO = new ImageJsonObject();
        sltO = new ImageJsonObject();
        slmO = new ImageJsonObject();
        slbO = new ImageJsonObject();
        srtO = new ImageJsonObject();
        srmO = new ImageJsonObject();
        srbO = new ImageJsonObject();
        preview = new ImageJsonObject();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private void setConfig(ImageView v, ImageJsonObject o) {
        boolean thisAreaLocked = false;

        //set locked areas
        if(!preferences.getBoolean(getString(R.string.unlocked_pref),false)) {
            switch (o.position) {
                case top_left_corner:
                case top_left_middle:
                case top_right_corner:
                case top_right_middle:
                case side_left_bottom:
                case side_left_middle:
                case side_left_top:
                case side_right_top:
                    thisAreaLocked = true;
            }
        }

        v.getLayoutParams().width = o.width;
        v.getLayoutParams().height = o.height;
        if (editMode) {
            //alternate colors
            int color = getResources().getColor(R.color.edit_box_light);

            if(thisAreaLocked)
                color = getResources().getColor(R.color.edit_box_super_light);

            switch (o.position) {
                case top_left_middle:
                case top_right_corner:
                case side_left_top:
                case side_left_bottom:
                case side_right_middle:
                case bottom_left_middle:
                case bottom_right_corner:
                    if(thisAreaLocked)
                        color = getResources().getColor(R.color.edit_box_very_light);
                    else
                        color = getResources().getColor(R.color.edit_box_regular);
            }

            v.setBackgroundColor(color);
        }

        //built in themes
        if(preferences.getInt(getString(R.string.theme_id),1) > 1) {
            try {
                String asset = ThemeJsonObject.getFileFromPosition(currTheme, o.position);
                if(asset.toLowerCase().equals("error")) throw new Exception("Image cannot be loaded, bad position");
                BitmapDrawable b = ((BitmapDrawable)v.getDrawable());
                if(b!=null) b.getBitmap().recycle();
                v.setImageDrawable(
                    Drawable.createFromStream(
                        getAssets().open(
                            asset
                        ),
                        null
                    )
                );
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        //custom themes
        } else {
            if (!thisAreaLocked) {
                try {
                    String asset = ThemeJsonObject.getFileFromPosition(currTheme, o.position);
                    if (asset.toLowerCase().equals("error"))
                        throw new Exception("Image cannot be loaded, bad position");

                    BitmapDrawable b = ((BitmapDrawable) v.getDrawable());
                    if (b != null) b.getBitmap().recycle();
                    v.setImageBitmap(decodeSampledBitmapFromFile(ThemeJsonObject.getCustomThemePath(currTheme) + "/" + asset, o.width, o.height));
                } catch (Exception e) {
                    if(e.getMessage() != null)
                        Log.e("error", e.getMessage());
                }
            }
        }

        v.requestLayout();
    }

    private void setSizes() {
        //set initial screen size
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density  = getResources().getDisplayMetrics().density;
        startWindowHeight = outMetrics.heightPixels / density;
        startWindowWidth  = outMetrics.widthPixels / density;

        float thicknessDP = getResources().getDimension(R.dimen.barThickness) / density;
        float cornerDP = thicknessDP * 2;
        int hSplitPX = (int)Math.round(((startWindowHeight - 2*cornerDP) / 3.0) * density);
        int wSplitPX = (int)Math.round(((startWindowWidth - 2*cornerDP) / 2.0) * density);
        int cornerPX = (int)(cornerDP * density);
        int thicknessPX = (int)(thicknessDP * density);

        Log.d("dimens","thickness: "+thicknessPX);
        Log.d("dimens","hsplit: "+hSplitPX);
        Log.d("dimens","wsplit: "+wSplitPX);
        Log.d("dimens","corner: "+cornerPX);
        Log.d("dimens","screenh: "+outMetrics.heightPixels);

        currTheme = ThemeJsonObject.getTheme(this,preferences.getInt(getString(R.string.theme_id),1));
        if(currTheme.id == 1) {
            currTheme = ThemeJsonObject.getCustomTheme(this,preferences.getInt(getString(R.string.custom_id),1));
        }

        preview.setDefaults(this, ImageJsonObject.Position.preview, (int)getResources().getDimension(R.dimen.previewWidth), (int)getResources().getDimension(R.dimen.previewHeight), "", ImageJsonObject.SizeType.rectangle);

        //corners
        tlcO.setDefaults(this, ImageJsonObject.Position.top_left_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square);
        setConfig(tlcV, tlcO);

        trcO.setDefaults(this, ImageJsonObject.Position.top_right_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square);
        setConfig(trcV, trcO);

        blcO.setDefaults(this, ImageJsonObject.Position.bottom_left_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square);
        setConfig(blcV, blcO);

        brcO.setDefaults(this, ImageJsonObject.Position.bottom_right_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square);
        setConfig(brcV, brcO);

        //top and bottom
        tlmO.setDefaults(this, ImageJsonObject.Position.top_left_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle);
        setConfig(tlmV, tlmO);

        trmO.setDefaults(this, ImageJsonObject.Position.top_right_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle);
        setConfig(trmV, trmO);

        blmO.setDefaults(this, ImageJsonObject.Position.bottom_left_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle);
        setConfig(blmV, blmO);

        brmO.setDefaults(this, ImageJsonObject.Position.bottom_right_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle);
        setConfig(brmV, brmO);

        //left side
        slbO.setDefaults(this, ImageJsonObject.Position.side_left_bottom, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle);
        setConfig(slbV, slbO);

        slmO.setDefaults(this, ImageJsonObject.Position.side_left_middle, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle);
        setConfig(slmV, slmO);

        sltO.setDefaults(this, ImageJsonObject.Position.side_left_top, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle);
        setConfig(sltV, sltO);

        //right side
        srbO.setDefaults(this, ImageJsonObject.Position.side_right_bottom, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle);
        setConfig(srbV, srbO);

        srmO.setDefaults(this, ImageJsonObject.Position.side_right_middle, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle);
        setConfig(srmV, srmO);

        srtO.setDefaults(this, ImageJsonObject.Position.side_right_top, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle);
        setConfig(srtV, srtO);
    }

    private View.OnSystemUiVisibilityChangeListener systemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            Log.d("layout","systemuichange");
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            Log.d("layout",Integer.toString(visibility)+ " "+ Integer.toString(View.SYSTEM_UI_FLAG_FULLSCREEN)+" "+ Integer.toString(View.SYSTEM_UI_FLAG_LOW_PROFILE)+" "+Boolean.toString((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0));
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0
                && (visibility & View.SYSTEM_UI_FLAG_LOW_PROFILE) == 0) {
                //system bars are visible
                showViews();
            } else {
                // The system bars are NOT visible.
                if(preferences.getBoolean(getString(R.string.system_fullscreen_pref),false)) {
                    hideViews();
                }
            }
        }
    };

    /*
    ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Log.d("layout","global layout change");
            Rect r = new Rect();
            serviceView.getWindowVisibleDisplayFrame(r);
            int screenHeight = serviceView.getRootView().getHeight();

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            int keypadHeight = screenHeight - r.bottom;

            Log.d("service", "keypadHeight = " + keypadHeight);

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                Log.d("service","keyboard open");
            }
            else {
                Log.d("service","keyboard closed");
            }
        }
    };
    */

    @Override public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences(getString(R.string.pref_namespace),MODE_PRIVATE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        serviceView = (RelativeLayout) inflater.inflate(R.layout.service_view, null);

        //detect full screen apps
        serviceView.setOnSystemUiVisibilityChangeListener(systemUiVisibilityChangeListener);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                //WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;

        windowManager.addView(serviceView, params);

        //detect system bar pulldown - accessibility service now...
        //serviceView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);

        runningInstance = this;

        initVars();

        //detect screen lock
        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(Intent.ACTION_SCREEN_ON);
        theFilter.addAction(Intent.ACTION_SCREEN_OFF);
        getApplicationContext().registerReceiver(screenOnOffReceiver, theFilter);

        /*gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        gestureLibrary.load();

        gestureOverlayView = (GestureOverlayView)serviceView.findViewById(R.id.gestures);
        gestureOverlayView.addOnGesturePerformedListener(gesturePerformedListener);*/
    }

    private void hideViews(){
        trcV.setVisibility(View.INVISIBLE);
        tlcV.setVisibility(View.INVISIBLE);
        brcV.setVisibility(View.INVISIBLE);
        blcV.setVisibility(View.INVISIBLE);
        // top and bottom
        brmV.setVisibility(View.INVISIBLE);
        blmV.setVisibility(View.INVISIBLE);
        trmV.setVisibility(View.INVISIBLE);
        tlmV.setVisibility(View.INVISIBLE);
        //left side
        slbV.setVisibility(View.INVISIBLE);
        slmV.setVisibility(View.INVISIBLE);
        sltV.setVisibility(View.INVISIBLE);
        //right side
        srbV.setVisibility(View.INVISIBLE);
        srmV.setVisibility(View.INVISIBLE);
        srtV.setVisibility(View.INVISIBLE);
    }

    private void showViews(){
        trcV.setVisibility(View.VISIBLE);
        tlcV.setVisibility(View.VISIBLE);
        brcV.setVisibility(View.VISIBLE);
        blcV.setVisibility(View.VISIBLE);
        // top and bottom
        brmV.setVisibility(View.VISIBLE);
        blmV.setVisibility(View.VISIBLE);
        trmV.setVisibility(View.VISIBLE);
        tlmV.setVisibility(View.VISIBLE);
        //left side
        slbV.setVisibility(View.VISIBLE);
        slmV.setVisibility(View.VISIBLE);
        sltV.setVisibility(View.VISIBLE);
        //right side
        srbV.setVisibility(View.VISIBLE);
        srmV.setVisibility(View.VISIBLE);
        srtV.setVisibility(View.VISIBLE);
    }

    private BroadcastReceiver screenOnOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String strAction = intent.getAction();
            if (strAction.equals(Intent.ACTION_SCREEN_OFF) || strAction.equals(Intent.ACTION_USER_PRESENT))
            {
                Log.d("XXXXXXXXXXXxx",strAction);
                if(strAction.equals(Intent.ACTION_SCREEN_OFF))
                {
                    Log.d("XXXXXXXXXXXxx","screen locked");
                    if(preferences.getBoolean(getString(R.string.lockscreen_pref),false)) {
                        hideViews();
                    } else {
                        showViews();
                    }
                }
                else
                {
                    Log.d("XXXXXXXXXXXxx","screen unlocked");
                    showViews();
                }
            }
        }
    };

    /*GestureOverlayView.OnGesturePerformedListener gesturePerformedListener = new GestureOverlayView.OnGesturePerformedListener(){
        @Override
        public void onGesturePerformed(GestureOverlayView view, Gesture gesture) {
            // TODO Auto-generated method stub
            ArrayList<Prediction> prediction = gestureLibrary.recognize(gesture);
            Log.d("gesture-found","yup");
            if(prediction.size() > 0){
                Log.d("gesture",prediction.get(0).name);
            }
        }
    };*/

    private void checkSpecificPos(ImageJsonObject.Position p) {
        switch (p) {
            case    top_left_corner: tlcV.setBackgroundColor(getResources().getColor(R.color.color_accent)); tlcV.requestLayout(); break;
            case    top_left_middle: tlmV.setBackgroundColor(getResources().getColor(R.color.color_accent)); tlmV.requestLayout(); break;
            case    top_right_middle: trmV.setBackgroundColor(getResources().getColor(R.color.color_accent)); trmV.requestLayout(); break;
            case    top_right_corner: trcV.setBackgroundColor(getResources().getColor(R.color.color_accent)); trcV.requestLayout(); break;

            case    bottom_left_corner: blcV.setBackgroundColor(getResources().getColor(R.color.color_accent)); blcV.requestLayout(); break;
            case    bottom_left_middle: blmV.setBackgroundColor(getResources().getColor(R.color.color_accent)); blmV.requestLayout(); break;
            case    bottom_right_middle: brmV.setBackgroundColor(getResources().getColor(R.color.color_accent)); brmV.requestLayout(); break;
            case    bottom_right_corner: brcV.setBackgroundColor(getResources().getColor(R.color.color_accent)); brcV.requestLayout(); break;

            case    side_left_top: sltV.setBackgroundColor(getResources().getColor(R.color.color_accent)); sltV.requestLayout(); break;
            case    side_left_middle: slmV.setBackgroundColor(getResources().getColor(R.color.color_accent)); slmV.requestLayout(); break;
            case    side_left_bottom: slbV.setBackgroundColor(getResources().getColor(R.color.color_accent)); slbV.requestLayout(); break;

            case    side_right_top: srtV.setBackgroundColor(getResources().getColor(R.color.color_accent)); srtV.requestLayout(); break;
            case    side_right_middle: srmV.setBackgroundColor(getResources().getColor(R.color.color_accent)); srmV.requestLayout(); break;
            case    side_right_bottom: srbV.setBackgroundColor(getResources().getColor(R.color.color_accent)); srbV.requestLayout(); break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(screenOnOffReceiver);
        if (serviceView != null) {
            windowManager.removeView(serviceView);
        }
        runningInstance = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        boolean doingScreenshot = false;

        if(intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                editMode = extras.get(getString(R.string.editModeExtra)) != null ? (Boolean) extras.get(getString(R.string.editModeExtra)) : false;
                editPos =  extras.get(getString(R.string.editPosExtra)) != null ? (String) extras.get(getString(R.string.editPosExtra)) : "";
                if(extras.get(getString(R.string.screenshotExtra)) != null && (Boolean)extras.get(getString(R.string.screenshotExtra))) {
                    doingScreenshot = true;
                }
            }
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH && pm.isInteractive()) {
            Log.d("XXXXXXXXXXXxx", "show views");
            showViews();
        } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT_WATCH && pm.isScreenOn()) {
            Log.d("XXXXXXXXXXXxx", "show views");
            showViews();
        } else {
            Log.d("XXXXXXXXXXXxx","hide views");
            if(preferences.getBoolean(getString(R.string.lockscreen_pref),false)) {
                hideViews();
            } else {
                showViews();
            }
        }

        setSizes();

        if(editPos != null && !editPos.equals(""))
            checkSpecificPos(ImageJsonObject.Position.valueOf(editPos));

        Log.d("screenshot","inservice: "+Boolean.toString(doingScreenshot));
        if(doingScreenshot){
            serviceView.findViewById(R.id.screenshot_image).setVisibility(View.VISIBLE);
            Intent i = new Intent(getString(R.string.intent_filter_service_ready));
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        } else {
            serviceView.findViewById(R.id.screenshot_image).setVisibility(View.INVISIBLE);
        }

        return START_STICKY;
    }
}