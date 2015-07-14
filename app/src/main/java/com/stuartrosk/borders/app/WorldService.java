package com.stuartrosk.borders.app;

import android.app.Activity;
import android.app.Service;
import android.content.*;
import android.gesture.*;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

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
            srtO, srmO, srbO;
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
    }

    private void setConfig(ImageView v, ImageJsonObject o) {
        v.getLayoutParams().width = o.width;
        v.getLayoutParams().height = o.height;
        if (editMode) {
            int color = getResources().getColor(R.color.edit_box_light);
            switch (o.position) {
                case top_left_middle:
                case top_right_corner:
                case side_left_top:
                case side_left_bottom:
                case side_right_middle:
                case bottom_left_middle:
                case bottom_right_corner:
                    color = getResources().getColor(R.color.edit_box_regular);
            }
            v.setBackgroundColor(color);
        }

        if(currTheme.id != 1) {
            try {
                String asset = ThemeJsonObject.getFileFromPosition(currTheme, o.position);
                if(asset.toLowerCase().equals("error")) throw new Exception("Image cannot be loaded, bad position");
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
        } else {
            v.setImageURI(o.getFullPath(this));
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
        int hSplitPX = (int)(((startWindowHeight - 2*cornerDP) / 3) * density);
        int wSplitPX = (int)(((startWindowWidth - 2*cornerDP) / 2) * density);
        int cornerPX = (int)(cornerDP * density);
        int thicknessPX = (int)(thicknessDP * density);

        currTheme = ThemeJsonObject.getTheme(this,preferences.getInt(getString(R.string.theme_id),1));

        /////force resetting defaults config for testing only
        boolean resetPrefs = false;
        /////end testing config

        //corners
        tlcO.setDefaults(this, ImageJsonObject.Position.top_left_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square, resetPrefs);
        setConfig(tlcV, tlcO);

        trcO.setDefaults(this, ImageJsonObject.Position.top_right_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square, resetPrefs);
        setConfig(trcV, trcO);

        blcO.setDefaults(this, ImageJsonObject.Position.bottom_left_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square, resetPrefs);
        setConfig(blcV, blcO);

        brcO.setDefaults(this, ImageJsonObject.Position.bottom_right_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square, resetPrefs);
        setConfig(brcV, brcO);

        //top and bottom
        tlmO.setDefaults(this, ImageJsonObject.Position.top_left_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        setConfig(tlmV, tlmO);

        trmO.setDefaults(this, ImageJsonObject.Position.top_right_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        setConfig(trmV, trmO);

        blmO.setDefaults(this, ImageJsonObject.Position.bottom_left_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        setConfig(blmV, blmO);

        brmO.setDefaults(this, ImageJsonObject.Position.bottom_right_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        setConfig(brmV, brmO);

        //left side
        slbO.setDefaults(this, ImageJsonObject.Position.side_left_bottom, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        setConfig(slbV, slbO);

        slmO.setDefaults(this, ImageJsonObject.Position.side_left_middle, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        setConfig(slmV, slmO);

        sltO.setDefaults(this, ImageJsonObject.Position.side_left_top, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        setConfig(sltV, sltO);

        //right side
        srbO.setDefaults(this, ImageJsonObject.Position.side_right_bottom, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        setConfig(srbV, srbO);

        srmO.setDefaults(this, ImageJsonObject.Position.side_right_middle, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        setConfig(srmV, srmO);

        srtO.setDefaults(this, ImageJsonObject.Position.side_right_top, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        setConfig(srtV, srtO);
    }

    @Override public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences(getString(R.string.pref_namespace),MODE_PRIVATE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        serviceView = (RelativeLayout) inflater.inflate(R.layout.service_view, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                //WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;

        windowManager.addView(serviceView, params);

        runningInstance = this;

        initVars();

        /*gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        gestureLibrary.load();

        gestureOverlayView = (GestureOverlayView)serviceView.findViewById(R.id.gestures);
        gestureOverlayView.addOnGesturePerformedListener(gesturePerformedListener);*/
    }

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
            case    top_left_corner: tlcV.setBackgroundColor(Color.YELLOW); tlcV.requestLayout(); break;
            case    top_left_middle: tlmV.setBackgroundColor(Color.YELLOW); tlmV.requestLayout(); break;
            case    top_right_middle: trmV.setBackgroundColor(Color.YELLOW); trmV.requestLayout(); break;
            case    top_right_corner: trcV.setBackgroundColor(Color.YELLOW); trcV.requestLayout(); break;

            case    bottom_left_corner: blcV.setBackgroundColor(Color.YELLOW); blcV.requestLayout(); break;
            case    bottom_left_middle: blmV.setBackgroundColor(Color.YELLOW); blmV.requestLayout(); break;
            case    bottom_right_middle: brmV.setBackgroundColor(Color.YELLOW); brmV.requestLayout(); break;
            case    bottom_right_corner: brcV.setBackgroundColor(Color.YELLOW); brcV.requestLayout(); break;

            case    side_left_top: sltV.setBackgroundColor(Color.YELLOW); sltV.requestLayout(); break;
            case    side_left_middle: slmV.setBackgroundColor(Color.YELLOW); slmV.requestLayout(); break;
            case    side_left_bottom: slbV.setBackgroundColor(Color.YELLOW); slbV.requestLayout(); break;

            case    side_right_top: srtV.setBackgroundColor(Color.YELLOW); srtV.requestLayout(); break;
            case    side_right_middle: srmV.setBackgroundColor(Color.YELLOW); srmV.requestLayout(); break;
            case    side_right_bottom: srbV.setBackgroundColor(Color.YELLOW); srbV.requestLayout(); break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceView != null) {
            windowManager.removeView(serviceView);
        }
        runningInstance = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if(intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                editMode = extras.get("editMode") != null ? (Boolean) extras.get("editMode") : false;
                editPos =  extras.get("editPos") != null ? (String) extras.get("editPos") : "";
                if(extras.get("screenshot") != null && (Boolean)extras.get("screenshot")) {
                    serviceView.findViewById(R.id.screenshot_image).setVisibility(View.VISIBLE);
                    Intent i = new Intent("service-ready");
                    // add data
                    //i.putExtra("message", "data");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                }
            }
        }

        setSizes();

        if(editPos != null && !editPos.equals(""))
            checkSpecificPos(ImageJsonObject.Position.valueOf(editPos));

        return START_STICKY;
    }
}