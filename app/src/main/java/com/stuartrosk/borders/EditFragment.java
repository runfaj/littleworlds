package com.stuartrosk.borders;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
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
    private TableLayout custom_cont;
    private ImageView shareButton;
    private ImageView editButton;
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
        void onFinishEdit();
        void onStartEdit();
        void hideEditScreen();
        boolean appPermissions(boolean requestPerms);
        void showCustomScreen();
        void startScreenshotWorldService();
        void shareApp();
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
        int radius = getEnclosingCircleRadius(cx, cy);
        Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, radius, 0);
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
        Button editDoneBtn = (Button) v.findViewById(R.id.editDoneBtn);
        ImageView addButton = (ImageView) v.findViewById(R.id.custom_add_btn);
        ImageView importButton = (ImageView) v.findViewById(R.id.custom_import_btn);
        shareButton = (ImageView)v.findViewById(R.id.custom_share_btn);
        editButton = (ImageView)v.findViewById(R.id.custom_edit_btn);
        themeListFragment = new ThemeListFragment();
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

                    // get the hypotenuse so the radius is from one corner to the other
                    int radius = (int) Math.hypot(right, bottom);

                    Animator reveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
                    reveal.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            listener.onStartEdit();
                            getThemesDir();
                        }
                    });
                    reveal.setInterpolator(new DecelerateInterpolator(2f));
                    reveal.setDuration((long)animationDuration);
                    reveal.start();
                }
            });
        } else {
            listener.onStartEdit();
            getThemesDir();
        }

        //init fragment
        /*getFragmentManager().beginTransaction()
            .replace(R.id.themeFragmentCont, themeListFragment)
        .commit();*/

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
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTheme();
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTheme();
            }
        });

        return v;
    }

    private void shareTheme() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Are you sure?");
        alertDialogBuilder
                .setMessage("Which theme option would you like to share?")
                .setNeutralButton("App Link", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.shareApp();
                    }
                })
                .setPositiveButton("Theme File", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ThemeJsonObject.Theme manifest = ThemeJsonObject.getCustomTheme(getActivity().getApplicationContext(), preferences.getInt(getString(R.string.custom_id),1));
                        String result = ThemeJsonObject.zipCustomTheme(getActivity(), manifest);
                        if(result.equals(""))
                            Toast.makeText(getActivity().getApplicationContext(), "Error exporting theme. Please make sure there are no extra folders in the theme directory.", Toast.LENGTH_SHORT).show();
                        else {
                            Toast.makeText(getActivity().getApplicationContext(),"Success! Your theme can be found here: " + result,Toast.LENGTH_LONG).show();

                            PackageManager pm = getActivity().getPackageManager();
                            String installer = pm.getInstallerPackageName(getActivity().getApplicationContext().getPackageName());

                            String message = getString(R.string.share_theme_gp_free);
                            if(MainActivity.isPaidVersion(getActivity().getApplicationContext()))
                                message = getString(R.string.share_theme_gp_paid);
                            if(installer != null && !installer.equals("") && installer.contains("amazon")) {
                                message = getString(R.string.share_theme_azn_free);
                                if(MainActivity.isPaidVersion(getActivity().getApplicationContext()))
                                    message = getString(R.string.share_theme_azn_paid);
                            }

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            Uri uri = Uri.fromFile(new File(result));
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.putExtra(Intent.EXTRA_TEXT, message);
                            startActivity(Intent.createChooser(intent, "Share Borders theme to..."));
                        }
                    }
                })
                .setNegativeButton("Screenshot", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.startScreenshotWorldService();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void addNewTheme() {
        boolean error = false;

        File bordersDir = getThemesDir();

        if(bordersDir == null) {
            return;
        }

        int newNum = 1;
        File f = new File(bordersDir.getAbsolutePath());
        File[] files = f.listFiles();
        if(files.length > 0) {
            File largest = files[0];
            for (File inFile : files) {
                if (inFile.isDirectory() && inFile.getAbsolutePath().compareTo(largest.getAbsolutePath()) == 1) {
                    largest = inFile;
                }
            }
            newNum = Integer.parseInt(largest.getName());
            newNum++;
        }

        File temp3 = new File(bordersDir.getAbsolutePath() + "/" + newNum);
        if(!temp3.exists() || !temp3.isDirectory())
            temp3.mkdir();
        bordersDir = temp3;

        String manifestPath = bordersDir.getAbsolutePath() + "/manifest.json";
        File newManifest = new File(manifestPath);
        String newJson = "{title: 'Custom Theme "+newNum+"', file_prefix: 'my_border'}";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(newManifest));
            writer.write(newJson);
            writer.close();

            ThemeJsonObject.copyAsset(getActivity(),bordersDir.getAbsolutePath(), "README.txt");
        }
        catch (IOException e) {
            Toast.makeText(getActivity().getApplicationContext(), "Unable to write manifest. Is your external storage directory locked?",Toast.LENGTH_LONG).show();
            Log.e("Exception", "File write failed: " + e.toString());
            error = true;
        }

        if(!error) {
            ThemeJsonObject.Theme manifest = ThemeJsonObject.getThemeFromFile(getActivity().getApplicationContext(),manifestPath); //don't remove, may do additional checks
            themeListFragment.refreshCustom();
            themeListFragment.setCustomEntry(newNum);

            listener.showCustomScreen();
        }
    }

    public void updateThemeList(int index, String name) {
        if(themeListFragment != null) {
            preferences.edit()
                .putInt(getString(R.string.theme_id), index)
                .putString(getString(R.string.theme_key), name)
            .commit();
            themeListFragment.setThemeEntry(index);
        }
    }

    private String unpackZip(String inFullPath, String outFullPath)
    {
        //// returns path to theme manifest

        if(!outFullPath.equals("") && outFullPath.charAt(outFullPath.length() -1) != '/')
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
            Toast.makeText(getActivity().getApplicationContext(),"Error unpacking imported theme.",Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return "";
        }

        return outFullPath + "manifest.json";
    }

    public File getThemesDir(){
        File bordersDir = null;

        try {
            //create Borders dir
            bordersDir = Environment.getExternalStorageDirectory();
            File temp = new File(bordersDir.getAbsolutePath() + "/Borders");
            if (!temp.exists() || !temp.isDirectory())
                temp.mkdir();
            bordersDir = temp;

            //add readme to borders dir
            File readmeFile = new File(bordersDir.getAbsolutePath() + "/README.txt");
            if(!readmeFile.exists()) {
                ThemeJsonObject.copyAsset(getActivity(),bordersDir.getAbsolutePath(), "README.txt");
            }

            //add template file to borders dir
            File templateFile = new File(bordersDir.getAbsolutePath() + "/border_template.ai");
            if(!templateFile.exists()) {
                ThemeJsonObject.copyAsset(getActivity(), bordersDir.getAbsolutePath(), "border_template.ai");
            }

            //add Themes dir
            File temp2 = new File(bordersDir.getAbsolutePath() + "/Themes");
            if (!temp2.exists() || !temp2.isDirectory())
                temp2.mkdir();
            bordersDir = temp2;

        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), "Unable to create or retrieve themes directory. Corrupted or locked external storage directory",Toast.LENGTH_LONG).show();
        }

        return bordersDir;
    }

    public boolean importTheme(File file) {
        File bordersDir = getThemesDir();

        if(bordersDir == null) {
            return false;
        }

        int newNum = 1;
        File f = new File(bordersDir.getAbsolutePath());
        File[] files = f.listFiles();
        if(files.length > 0) {
            File largest = files[0];
            for (File inFile : files) {
                if (inFile.isDirectory() && inFile.getAbsolutePath().compareTo(largest.getAbsolutePath()) == 1) {
                    largest = inFile;
                }
            }
            newNum = Integer.parseInt(largest.getName());
            newNum++;
        }

        File temp3 = new File(bordersDir.getAbsolutePath() + "/" + newNum);
        if(!temp3.exists() || !temp3.isDirectory())
            temp3.mkdir();
        bordersDir = temp3;

        String manifestPath = unpackZip(file.getAbsolutePath(), bordersDir.getAbsolutePath());
        if(!manifestPath.equals("")) {
            ThemeJsonObject.Theme manifest = ThemeJsonObject.getThemeFromFile(getActivity().getApplicationContext(),manifestPath);
            if(manifest != null) {
                themeListFragment.refreshCustom();
                themeListFragment.setCustomEntry(newNum);
                Toast.makeText(getActivity().getApplicationContext(), "\"" + manifest.title + "\" imported!", Toast.LENGTH_LONG).show();
            }
            return true;
        } else {
            return false;
        }
    }

    private void importCustomTheme(){
        Toast.makeText(getActivity().getApplicationContext(),"Select a btheme file to import.",Toast.LENGTH_LONG).show();

        String[] extensions = {"btheme"};
        FileDialog fd = new FileDialog(getActivity(), "/", extensions, new FileDialog.FileDialogListener() {
            @Override
            public void fileDialogOutput(String path, String name, String ext) {
                File file = new File(path+"/"+name);
                importTheme(file);
            }
        });
        fd.show();
    }

    public void hideCustomCont(){
        custom_cont.setVisibility(View.INVISIBLE);
        custom_cont.requestLayout();
    }
    public void showCustomCont(){
        custom_cont.setVisibility(View.VISIBLE);
        custom_cont.requestLayout();

        String emptyText = getString(R.string.empty_custom_list_text);

        if(preferences.getString(getString(R.string.custom_key),emptyText).equals(emptyText)) {
            shareButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
        } else {
            shareButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
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
        customOnResume();
        super.onResume();
    }

    public void customOnResume() {
        if(listener.appPermissions(false)) {
            listener.onStartEdit();
            if(preferences.getInt(getString(R.string.theme_id),2) == 1)
                showCustomCont();
            else
                hideCustomCont();

            if(themeListFragment != null) {
                themeListFragment = new ThemeListFragment();
                //init fragment
                getFragmentManager().beginTransaction()
                        .replace(R.id.themeFragmentCont, themeListFragment)
                        .commit();
            }

        } else {
            listener.hideEditScreen();
        }
    }
}
