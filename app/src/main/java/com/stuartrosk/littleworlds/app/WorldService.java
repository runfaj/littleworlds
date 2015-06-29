package com.stuartrosk.littleworlds.app;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;

public class WorldService extends Service {

    private WindowManager windowManager;
    private RelativeLayout serviceView;
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

    private boolean editMode;
    private String editPos;

    @Override public IBinder onBind(Intent intent) {
        if(intent.getStringExtra("showEditMode").equals("true")){
            //show borders
        }
        return null;
    }

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

        /////force resetting defaults config for testing only
        boolean resetPrefs = false;
        /////end testing config

        //corners
        tlcO.setDefaults(this, ImageJsonObject.Position.top_left_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square, resetPrefs);
        tlcV.getLayoutParams().width = tlcV.getLayoutParams().height = tlcO.height;
        tlcV.setImageURI(tlcO.getFullPath(this));
        if(editMode)tlcV.setBackgroundColor(getResources().getColor(R.color.edit_box_light));
        tlcV.requestLayout();

        trcO.setDefaults(this, ImageJsonObject.Position.top_right_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square, resetPrefs);
        trcV.getLayoutParams().width = trcV.getLayoutParams().height = trcO.height;
        trcV.setImageURI(trcO.getFullPath(this));
        if(editMode)trcV.setBackgroundColor(getResources().getColor(R.color.edit_box_regular));
        trcV.requestLayout();

        blcO.setDefaults(this, ImageJsonObject.Position.bottom_left_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square, resetPrefs);
        blcV.getLayoutParams().width = blcV.getLayoutParams().height = blcO.height;
        blcV.setImageURI(blcO.getFullPath(this));
        if(editMode)blcV.setBackgroundColor(getResources().getColor(R.color.edit_box_light));
        blcV.requestLayout();

        brcO.setDefaults(this, ImageJsonObject.Position.bottom_right_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square, resetPrefs);
        brcV.getLayoutParams().width = brcV.getLayoutParams().height = brcO.height;
        brcV.setImageURI(brcO.getFullPath(this));
        if(editMode)brcV.setBackgroundColor(getResources().getColor(R.color.edit_box_regular));
        brcV.requestLayout();

        //top and bottom
        tlmO.setDefaults(this, ImageJsonObject.Position.top_left_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        tlmV.getLayoutParams().width = tlmO.width;
        tlmV.getLayoutParams().height = tlmO.height;
        tlmV.setImageURI(tlmO.getFullPath(this));
        if(editMode)tlmV.setBackgroundColor(getResources().getColor(R.color.edit_box_regular));
        tlmV.requestLayout();

        trmO.setDefaults(this, ImageJsonObject.Position.top_right_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        trmV.getLayoutParams().width = trmO.width;
        trmV.getLayoutParams().height = trmO.height;
        trmV.setImageURI(trmO.getFullPath(this));
        if(editMode)trmV.setBackgroundColor(getResources().getColor(R.color.edit_box_light));
        trmV.requestLayout();

        blmO.setDefaults(this, ImageJsonObject.Position.bottom_left_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        blmV.getLayoutParams().width = blmO.width;
        blmV.getLayoutParams().height = blmO.height;
        blmV.setImageURI(blmO.getFullPath(this));
        if(editMode)blmV.setBackgroundColor(getResources().getColor(R.color.edit_box_regular));
        blmV.requestLayout();

        brmO.setDefaults(this, ImageJsonObject.Position.bottom_right_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        brmV.getLayoutParams().width = brmO.width;
        brmV.getLayoutParams().height = brmO.height;
        brmV.setImageURI(brmO.getFullPath(this));
        if(editMode)brmV.setBackgroundColor(getResources().getColor(R.color.edit_box_light));
        brmV.requestLayout();

        //left side
        slbO.setDefaults(this, ImageJsonObject.Position.side_left_bottom, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        slbV.getLayoutParams().height = slbO.height;
        slbV.getLayoutParams().width = slbO.width;
        slbV.setImageURI(slbO.getFullPath(this));
        if(editMode)slbV.setBackgroundColor(getResources().getColor(R.color.edit_box_regular));
        slbV.requestLayout();

        slmO.setDefaults(this, ImageJsonObject.Position.side_left_middle, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        slmV.getLayoutParams().height = slmO.height;
        slmV.getLayoutParams().width = slmO.width;
        slmV.setImageURI(slmO.getFullPath(this));
        if(editMode)slmV.setBackgroundColor(getResources().getColor(R.color.edit_box_light));
        slmV.requestLayout();

        sltO.setDefaults(this, ImageJsonObject.Position.side_left_top, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        sltV.getLayoutParams().height = sltO.height;
        sltV.getLayoutParams().width = sltO.width;
        sltV.setImageURI(sltO.getFullPath(this));
        if(editMode)sltV.setBackgroundColor(getResources().getColor(R.color.edit_box_regular));
        sltV.requestLayout();

        //right side
        srbO.setDefaults(this, ImageJsonObject.Position.side_right_bottom, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        srbV.getLayoutParams().height = srbO.height;
        srbV.getLayoutParams().width = srbO.width;
        srbV.setImageURI(srbO.getFullPath(this));
        if(editMode)srbV.setBackgroundColor(getResources().getColor(R.color.edit_box_light));
        srbV.requestLayout();

        srmO.setDefaults(this, ImageJsonObject.Position.side_right_middle, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        srmV.getLayoutParams().height = srmO.height;
        srmV.getLayoutParams().width = srmO.width;
        srmV.setImageURI(srmO.getFullPath(this));
        if(editMode)srmV.setBackgroundColor(getResources().getColor(R.color.edit_box_regular));
        srmV.requestLayout();

        srtO.setDefaults(this, ImageJsonObject.Position.side_right_top, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle, resetPrefs);
        srtV.getLayoutParams().height = srtO.height;
        srtV.getLayoutParams().width = srtO.width;
        srtV.setImageURI(srtO.getFullPath(this));
        if(editMode)srtV.setBackgroundColor(getResources().getColor(R.color.edit_box_light));
        srtV.requestLayout();
    }

    @Override public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences("com.stuartrosk.littleworlds",MODE_PRIVATE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        serviceView = (RelativeLayout) inflater.inflate(R.layout.service_view, null);

        /*WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);*/

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                //WindowManager.LayoutParams.TYPE_INPUT_METHOD |
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,// | WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;

        windowManager.addView(serviceView, params);

        initVars();
    }

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
        if (serviceView != null) windowManager.removeView(serviceView);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Bundle extras = intent.getExtras();
        if(extras != null)
        {
            editMode = (Boolean) extras.get("editMode");
            editPos = (String) extras.get("editPos");
        }

        setSizes();

        if(editPos != null && !editPos.equals(""))
            checkSpecificPos(ImageJsonObject.Position.valueOf(editPos));

        return START_NOT_STICKY;
    }
}