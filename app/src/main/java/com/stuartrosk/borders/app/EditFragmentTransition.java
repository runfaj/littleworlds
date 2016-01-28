package com.stuartrosk.borders.app;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.Arrays;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditFragmentTransition extends Fragment {

    public boolean skipCreateAnimation = false;
    private View v;
    private EditFragmentTransitionListener listener;
    private float animationX = 0,
            animationY = 0,
            animationDuration = 750;

    public interface EditFragmentTransitionListener {
        public void editTransitionOpenDone();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (EditFragmentTransitionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EditFragmentTransitionListener");
        }
    }

    public EditFragmentTransition() {
        // Required empty public constructor
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
        if(v == null)
            v = getView();
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
        v = inflater.inflate(R.layout.fragment_edit_transition, container, false);

        //animation for fragment opening
        if(!skipCreateAnimation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(final View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                           int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    int cx = (int)animationX;
                    int cy = (int)animationY;

                    // get the hypothenuse so the radius is from one corner to the other
                    int radius = (int) Math.hypot(right, bottom);

                    Animator reveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
                    reveal.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            listener.editTransitionOpenDone();
                        }
                    });
                    reveal.setInterpolator(new DecelerateInterpolator(2f));
                    reveal.setDuration((long)animationDuration);
                    reveal.start();
                }
            });
        }

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
    }
}
