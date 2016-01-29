package com.stuartrosk.borders.app;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Arrays;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditFragment extends Fragment {

    private SharedPreferences preferences;
    private Button editDoneBtn;
    private EditFragmentListener listener;
    private ThemeListFragment themeListFragment;
    private View v;
    private float animationX = 0,
                  animationY = 0,
                  animationDuration = 1250;

    public EditFragment() {
        // Required empty public constructor
    }

    public interface EditFragmentListener {
        public void onFinishEdit();
        public void onStartEdit();
        public void hideEditScreen();
        public void showImageEditScreen(String editPos);
        public boolean appPermissions(boolean requestPerms);
    }


    public void toggleEditIcons() {
        int state;
        //if a custom theme, then show the edit buttons
        if(preferences.getInt(getString(R.string.theme_id),1) == 1)
            state = View.VISIBLE;
        else
            state = View.INVISIBLE;
        if(v==null)return;
        RelativeLayout r = (RelativeLayout)v.findViewById(R.id.fragmentEdit);
        for(int i=0;i<r.getChildCount();i++) {
            if(r.getChildAt(i) instanceof ImageView) {
                r.getChildAt(i).setVisibility(state);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (EditFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EditFragmentListener");
        }
    }

    /**
     * Get the animator to unreveal the circle
     *
     * @return Animator object that will be used for the animation
     */
    @TargetApi(21)
    public Animator prepareUnrevealAnimator() {
        int cx = (int)animationX;
        int cy = (int)animationY;
        int radius = getEnclosingCircleRadius((int) cx, (int) cy);
        Animator anim = ViewAnimationUtils.createCircularReveal(v, (int) cx, (int) cy, radius, 0);
        anim.setInterpolator(new AccelerateInterpolator(2f));
        anim.setDuration((long)animationDuration);
        return anim;
    }

    /**
     * To be really accurate we have to start the circle on the furthest corner of the view
     *
     * @param cx center x of the circle
     * @param cy center y of the circle
     * @return the maximum radius
     */
    private int getEnclosingCircleRadius(int cx, int cy) {
        int realCenterX = cx + v.getLeft();
        int realCenterY = cy + v.getTop();
        int distanceTopLeft = (int) Math.hypot(realCenterX - v.getLeft(), realCenterY - v.getTop());
        int distanceTopRight = (int) Math.hypot(v.getRight() - realCenterX, realCenterY - v.getTop());
        int distanceBottomLeft = (int) Math.hypot(realCenterX - v.getLeft(), v.getBottom() - realCenterY);
        int distanceBottomRight = (int) Math.hypot(v.getRight() - realCenterX, v.getBottom() - realCenterY);

        Integer[] distances = new Integer[]{distanceTopLeft, distanceTopRight, distanceBottomLeft,
                distanceBottomRight};

        return Collections.max(Arrays.asList(distances));
    }

    public void setAnimationXY(float x, float y){
        animationX = x;
        animationY = y;
    }

    @TargetApi(21)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_edit, container, false);

        preferences = getActivity().getSharedPreferences(getString(R.string.pref_namespace), getActivity().MODE_PRIVATE);
        editDoneBtn = (Button)v.findViewById(R.id.editDoneBtn);
        themeListFragment = new ThemeListFragment();

        //animation for fragment opening
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(final View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                           int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    //int cx = getArguments().getInt("cx");
                    //int cy = getArguments().getInt("cy");
                    int cx = (int)animationX;
                    int cy = (int)animationY;

                    // get the hypothenuse so the radius is from one corner to the other
                    int radius = (int) Math.hypot(right, bottom);

                    Animator reveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
                    reveal.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            listener.onStartEdit();
                            super.onAnimationEnd(animation);
                        }
                    });
                    reveal.setInterpolator(new DecelerateInterpolator(2f));
                    reveal.setDuration((long)animationDuration);
                    reveal.start();
                }
            });
        }

        //init fragment
        getFragmentManager().beginTransaction()
            .replace(R.id.themeFragmentCont, themeListFragment)
        .commit();

        editDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.hideEditScreen();
            }
        });

        //set edit button handlers
        ViewGroup vg = (ViewGroup)v.findViewById(R.id.fragmentEdit);
        for(int i=0;i<vg.getChildCount();i++) {
            if(vg.getChildAt(i) instanceof ImageView) {
                testUnlocked(vg.getChildAt(i));
            }
        }

        return v;
    }

    //iterable
    private void testUnlocked(View v){
        //return true if still showing the button
        String pos = v.getTag().toString();
        if(pos == null) return;

        //only giving the user 6 options if its locked
        boolean lockIt = false;
        switch(pos){
            case "top_left_corner":
            case "top_right_corner":
            case "top_left_middle":
            case "top_right_middle":
            case "side_left_middle":
            case "side_left_top":
            case "side_left_bottom":
            case "side_right_top":
                lockIt = true; break;
        }

        if(preferences.getBoolean(getString(R.string.unlocked_pref),false))
            lockIt = false;

        if(lockIt) {
            v.setRotation(0F);
            ((ImageView) v).setImageResource(R.drawable.ic_lock_outline_black_48dp);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UnlockDialog unlockDialog = new UnlockDialog(view.getContext(),null);
                    unlockDialog.showDialog();
                }
            });
        } else {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String btnName = v.getTag().toString();
                    listener.showImageEditScreen(btnName);
                }
            });

        }
    }

    public void updatePreferenceList(int index) {
        if(themeListFragment != null) {
            preferences.edit()
                .putInt(getString(R.string.theme_id), index)
                .putString(getString(R.string.theme_key), "Custom")
            .commit();
            themeListFragment.updateThemeEntry(index);
        }
    }

    @Override
    public void onDestroy() {
        listener.onFinishEdit();

        super.onDestroy();
    }
    @Override
    public void onPause() {
        listener.onFinishEdit();

        super.onPause();
    }
    @Override
    public void onResume() {
        if(listener.appPermissions(false)) {
            listener.onStartEdit();
            toggleEditIcons();
        } else {
            listener.hideEditScreen();
        }
        super.onResume();
    }
}
