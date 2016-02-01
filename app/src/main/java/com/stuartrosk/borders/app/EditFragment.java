package com.stuartrosk.borders.app;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditFragment extends Fragment {

    private SharedPreferences preferences;
    private Button editDoneBtn;
    private TableLayout custom_cont;
    private ImageView addButton, shareButton, editButton, importButton;
    private EditFragmentListener listener;
    private ThemeListFragment themeListFragment;
    private CustomListFragment customListFragment;
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
        public boolean appPermissions(boolean requestPerms);
        public void showCustomScreen();
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
        addButton = (ImageView)v.findViewById(R.id.custom_add_btn); //////////////////////////////////////////////////////////////
        importButton = (ImageView)v.findViewById(R.id.custom_import_btn);
        shareButton = (ImageView)v.findViewById(R.id.custom_share_btn); //////////////////////////////////////////////////////////////
        editButton = (ImageView)v.findViewById(R.id.custom_edit_btn);
        themeListFragment = new ThemeListFragment();
        customListFragment = new CustomListFragment();
        custom_cont = (TableLayout)v.findViewById(R.id.custom_cont);

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
        } else {
            listener.onStartEdit();
        }

        //init fragment
        getFragmentManager().beginTransaction()
            .replace(R.id.themeFragmentCont, themeListFragment)
        .commit();

        getFragmentManager().beginTransaction()
            .replace(R.id.customFragmentCont, customListFragment)
        .commit();

        editDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.hideEditScreen();
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.showCustomScreen();
            }
        });
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importCustomTheme();
            }
        });

        return v;
    }

    public void updatePreferenceList(int index, String name) {
        if(themeListFragment != null) {
            preferences.edit()
                .putInt(getString(R.string.theme_id), index)
                .putString(getString(R.string.theme_key), name)
            .commit();
            themeListFragment.updateThemeEntry(index);
        }
    }

    private String unpackZip(String inFullPath, String outFullPath)
    {
        //// returns path to theme manifest

        if(outFullPath != "" && outFullPath.charAt(outFullPath.length() -1) != '/')
            outFullPath = outFullPath + "/";

        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(inFullPath);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(outFullPath + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(outFullPath + filename);

                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            Toast.makeText(getActivity().getApplicationContext(),"Error unpacking imported theme.",Toast.LENGTH_LONG);
            return "";
        }

        return outFullPath + "manifest.json";
    }

    public void importCustomTheme(){
        String[] extensions = {"btheme"};
        FileDialog fd = new FileDialog(getActivity(), "/", extensions, new FileDialog.FileDialogListener() {
            @Override
            public void fileDialogOutput(String path, String name, String ext) {
            File file = new File(path+"/"+name);

            File bordersDir = Environment.getExternalStorageDirectory();
            File temp = new File(bordersDir.getAbsolutePath() + "/Borders");
            if(!temp.exists() || !temp.isDirectory())
                temp.mkdir();
            bordersDir = temp;

            File temp2 = new File(bordersDir.getAbsolutePath() + "/Themes");
            if(!temp2.exists() || !temp2.isDirectory())
                temp2.mkdir();
            bordersDir = temp2;

            File f = new File(bordersDir.toString());
            File[] files = f.listFiles();
            File largest = files[0];
            for (File inFile : files) {
                if (inFile.isDirectory() && inFile.toString().compareTo(largest.toString()) == 1) {
                    largest = inFile;
                }
            }
            Log.d("ef reading",largest.toString());
            int newNum = Integer.parseInt(largest.toString());
            newNum++;

            File temp3 = new File(bordersDir.getAbsolutePath() + "/" + newNum);
            if(!temp3.exists() || !temp3.isDirectory())
                temp3.mkdir();
            bordersDir = temp3;

            String manifestPath = unpackZip(file.toString(), bordersDir.getAbsolutePath());
            if(manifestPath != "") {
                ThemeJsonObject.Theme manifest = ThemeJsonObject.getThemeFromFile(getActivity().getApplicationContext(),manifestPath);
                customListFragment.updateThemeEntry(newNum);
                Toast.makeText(getActivity().getApplicationContext(),"\""+ manifest.title +"\" imported!", Toast.LENGTH_LONG);
            }
            }
        });
        fd.show();
    }

    public void hideCustomCont(){
        custom_cont.setVisibility(View.INVISIBLE);
    }
    public void showCustomCont(){
        custom_cont.setVisibility(View.VISIBLE);
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
            if(preferences.getInt(getString(R.string.theme_id),2) == 1)
                showCustomCont();
            else
                hideCustomCont();
        } else {
            listener.hideEditScreen();
        }
        super.onResume();
    }
}
