package com.stuartrosk.littleworlds.app;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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

        //corners
        tlcO.setDefaults(this, ImageJsonObject.Position.top_left_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square);
        tlcV.getLayoutParams().width = tlcV.getLayoutParams().height = tlcO.height;
        tlcV.requestLayout();

        trcO.setDefaults(this, ImageJsonObject.Position.top_right_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square);
        trcV.getLayoutParams().width = trcV.getLayoutParams().height = trcO.height;
        trcV.requestLayout();

        blcO.setDefaults(this, ImageJsonObject.Position.bottom_left_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square);
        blcV.getLayoutParams().width = blcV.getLayoutParams().height = blcO.height;
        blcV.requestLayout();

        brcO.setDefaults(this, ImageJsonObject.Position.bottom_right_corner, cornerPX, cornerPX, "", ImageJsonObject.SizeType.square);
        brcV.getLayoutParams().width = brcV.getLayoutParams().height = brcO.height;
        brcV.requestLayout();

        //top and bottom
        tlmO.setDefaults(this, ImageJsonObject.Position.top_left_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle);
        tlmV.getLayoutParams().width = tlmO.width;
        tlmV.getLayoutParams().height = tlmO.height;
        tlmV.requestLayout();

        trmO.setDefaults(this, ImageJsonObject.Position.top_right_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle);
        trmV.getLayoutParams().width = trmO.width;
        trmV.getLayoutParams().height = trmO.height;
        trmV.requestLayout();

        blmO.setDefaults(this, ImageJsonObject.Position.bottom_left_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle);
        blmV.getLayoutParams().width = blmO.width;
        blmV.getLayoutParams().height = blmO.height;
        blmV.requestLayout();

        brmO.setDefaults(this, ImageJsonObject.Position.bottom_right_middle, wSplitPX, thicknessPX, "", ImageJsonObject.SizeType.rectangle);
        brmV.getLayoutParams().width = brmO.width;
        brmV.getLayoutParams().height = brmO.height;
        brmV.requestLayout();

        //left side
        slbO.setDefaults(this, ImageJsonObject.Position.side_left_bottom, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle);
        slbV.getLayoutParams().height = slbO.height;
        slbV.getLayoutParams().width = slbO.width;
        slbV.requestLayout();

        slmO.setDefaults(this, ImageJsonObject.Position.side_left_middle, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle);
        slmV.getLayoutParams().height = slmO.height;
        slmV.getLayoutParams().width = slmO.width;
        slmV.requestLayout();

        sltO.setDefaults(this, ImageJsonObject.Position.side_left_top, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle);
        sltV.getLayoutParams().height = sltO.height;
        sltV.getLayoutParams().width = sltO.width;
        sltV.requestLayout();

        //right side
        srbO.setDefaults(this, ImageJsonObject.Position.side_right_bottom, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle);
        srbV.getLayoutParams().height = srbO.height;
        srbV.getLayoutParams().width = srbO.width;
        srbV.requestLayout();

        srmO.setDefaults(this, ImageJsonObject.Position.side_right_middle, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle);
        srmV.getLayoutParams().height = srmO.height;
        srmV.getLayoutParams().width = srmO.width;
        srmV.requestLayout();

        srtO.setDefaults(this, ImageJsonObject.Position.side_right_top, thicknessPX, hSplitPX, "", ImageJsonObject.SizeType.rectangle);
        srtV.getLayoutParams().height = srtO.height;
        srtV.getLayoutParams().width = srtO.width;
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
        setSizes();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceView != null) windowManager.removeView(serviceView);
    }
}