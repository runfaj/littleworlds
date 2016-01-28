package com.stuartrosk.borders.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;

public class WelcomeFragment extends Fragment {

    private WelcomeFragmentListener listener;
    private View mainView;
    private Button mainButton;
    private SharedPreferences preferences;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    public interface WelcomeFragmentListener {
        public void showHomeScreenFromWelcome();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (WelcomeFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement WelcomeFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_welcome, container, false);

        mainButton = (Button)mainView.findViewById(R.id.welcome_button);
        preferences = getActivity().getSharedPreferences(getString(R.string.pref_namespace), getActivity().MODE_PRIVATE);

        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.showHomeScreenFromWelcome();
            }
        });

        setSizes();
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setSizes() {
        WindowManager windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        ImageView trcV, tlcV, brcV, blcV,
                tlmV, trmV, blmV, brmV,
                sltV, slmV, slbV,
                srtV, srmV, srbV;
        float startWindowWidth, startWindowHeight;

        View s = mainView;

        // corners
        trcV = (ImageView)s.findViewById(R.id.welcome_topRightCorner);
        tlcV = (ImageView)s.findViewById(R.id.welcome_topLeftCorner);
        brcV = (ImageView)s.findViewById(R.id.welcome_bottomRightCorner);
        blcV = (ImageView)s.findViewById(R.id.welcome_bottomLeftCorner);
        // top and bottom
        brmV = (ImageView)s.findViewById(R.id.welcome_bottomRightMiddle);
        blmV = (ImageView)s.findViewById(R.id.welcome_bottomLeftMiddle);
        trmV = (ImageView)s.findViewById(R.id.welcome_topRightMiddle);
        tlmV = (ImageView)s.findViewById(R.id.welcome_topLeftMiddle);
        //left side
        slbV = (ImageView)s.findViewById(R.id.welcome_sideLeftBottom);
        slmV = (ImageView)s.findViewById(R.id.welcome_sideLeftMiddle);
        sltV = (ImageView)s.findViewById(R.id.welcome_sideLeftTop);
        //right side
        srbV = (ImageView)s.findViewById(R.id.welcome_sideRightBottom);
        srmV = (ImageView)s.findViewById(R.id.welcome_sideRightMiddle);
        srtV = (ImageView)s.findViewById(R.id.welcome_sideRightTop);

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
        tlcV.getLayoutParams().width = cornerPX;
        tlcV.getLayoutParams().height = cornerPX;
        trcV.getLayoutParams().width = cornerPX;
        trcV.getLayoutParams().height = cornerPX;
        blcV.getLayoutParams().width = cornerPX;
        blcV.getLayoutParams().height = cornerPX;
        brcV.getLayoutParams().width = cornerPX;
        brcV.getLayoutParams().height = cornerPX;

        //top and bottom
        tlmV.getLayoutParams().width = wSplitPX;
        tlmV.getLayoutParams().height = thicknessPX;
        trmV.getLayoutParams().width = wSplitPX;
        trmV.getLayoutParams().height = thicknessPX;
        blmV.getLayoutParams().width = wSplitPX;
        blmV.getLayoutParams().height = thicknessPX;
        brmV.getLayoutParams().width = wSplitPX;
        brmV.getLayoutParams().height = thicknessPX;

        //left side
        slbV.getLayoutParams().width = thicknessPX;
        slbV.getLayoutParams().height = hSplitPX;
        slmV.getLayoutParams().width = thicknessPX;
        slmV.getLayoutParams().height = hSplitPX;
        sltV.getLayoutParams().width = thicknessPX;
        sltV.getLayoutParams().height = hSplitPX;

        //right side
        srbV.getLayoutParams().width = thicknessPX;
        srbV.getLayoutParams().height = hSplitPX;
        srmV.getLayoutParams().width = thicknessPX;
        srmV.getLayoutParams().height = hSplitPX;
        srtV.getLayoutParams().width = thicknessPX;
        srtV.getLayoutParams().height = hSplitPX;
    }
}
