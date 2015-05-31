package com.stuartrosk.littleworlds.app;

import android.app.Service;
import android.content.Intent;
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
    private ImageView trc, tlc, brc, blc,
            tlm, trm, blm, brm,
            slt, slm, slb,
            srt, srm, srb;
    private float startWindowWidth, startWindowHeight;

    @Override public IBinder onBind(Intent intent) {
        if(intent.getStringExtra("showEditMode").equals("true")){
            //show borders
        }
        return null;
    }

    private void initVars() {
        RelativeLayout s = serviceView;

        // corners
        trc = (ImageView)s.findViewById(R.id.topRightCorner);
        tlc = (ImageView)s.findViewById(R.id.topLeftCorner);
        brc = (ImageView)s.findViewById(R.id.bottomRightCorner);
        blc = (ImageView)s.findViewById(R.id.bottomLeftCorner);
        // top and bottom
        brm = (ImageView)s.findViewById(R.id.bottomRightMiddle);
        blm = (ImageView)s.findViewById(R.id.bottomLeftMiddle);
        trm = (ImageView)s.findViewById(R.id.topRightMiddle);
        tlm = (ImageView)s.findViewById(R.id.topLeftMiddle);
        //left side
        slb = (ImageView)s.findViewById(R.id.sideLeftBottom);
        slm = (ImageView)s.findViewById(R.id.sideLeftMiddle);
        slt = (ImageView)s.findViewById(R.id.sideLeftTop);
        //right side
        srb = (ImageView)s.findViewById(R.id.sideRightBottom);
        srm = (ImageView)s.findViewById(R.id.sideRightMiddle);
        srt = (ImageView)s.findViewById(R.id.sideRightTop);
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
        tlc.getLayoutParams().width = tlc.getLayoutParams().height = cornerPX;
        tlc.requestLayout();
        trc.getLayoutParams().width = trc.getLayoutParams().height = cornerPX;
        trc.requestLayout();
        blc.getLayoutParams().width = blc.getLayoutParams().height = cornerPX;
        blc.requestLayout();
        brc.getLayoutParams().width = brc.getLayoutParams().height = cornerPX;
        brc.requestLayout();
        //top and bottom
        tlm.getLayoutParams().width = wSplitPX;
        tlm.getLayoutParams().height = thicknessPX;
        tlm.requestLayout();
        trm.getLayoutParams().width = wSplitPX;
        trm.getLayoutParams().height = thicknessPX;
        trm.requestLayout();
        blm.getLayoutParams().width = wSplitPX;
        blm.getLayoutParams().height = thicknessPX;
        blm.requestLayout();
        brm.getLayoutParams().width = wSplitPX;
        brm.getLayoutParams().height = thicknessPX;
        brm.requestLayout();
        //left side
        slb.getLayoutParams().height = hSplitPX;
        slb.getLayoutParams().width = thicknessPX;
        slb.requestLayout();
        slm.getLayoutParams().height = hSplitPX;
        slm.getLayoutParams().width = thicknessPX;
        slm.requestLayout();
        slt.getLayoutParams().height = hSplitPX;
        slt.getLayoutParams().width = thicknessPX;
        slt.requestLayout();
        //right side
        srb.getLayoutParams().height = hSplitPX;
        srb.getLayoutParams().width = thicknessPX;
        srb.requestLayout();
        srm.getLayoutParams().height = hSplitPX;
        srm.getLayoutParams().width = thicknessPX;
        srm.requestLayout();
        srt.getLayoutParams().height = hSplitPX;
        srt.getLayoutParams().width = thicknessPX;
        srt.requestLayout();
    }

    @Override public void onCreate() {
        super.onCreate();

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