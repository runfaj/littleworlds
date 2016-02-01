package com.stuartrosk.borders.app;


import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private ImageButton mainEditButton;
    private Switch toggleSwitch;
    private SharedPreferences preferences;
    private HomeFragmentListener listener;
    private ImageView rank1, rank2, rank3, rank4, rank5;
    private View.OnClickListener bad_rating, good_rating;
    private Button shareBtn, settingsBtn, pointsBtn;
    private View mainView;
    private RelativeLayout ratingCont;
    private CompoundButton.OnCheckedChangeListener checkedChangeListener = null;

    public HomeFragment() {
        // Required empty public constructor
    }

    public interface HomeFragmentListener {
        public void startWorldService(boolean editMode, String editPos);
        public void stopWorldService();
        public void showEditScreen(float x, float y);
        public boolean isServiceRunning();
        public void firstTimer();
        public void startScreenshotWorldService();
        public void stopScreenshotWorldService();
        public void showSettings();
        public void onShowSharePopup();
        //public boolean requestOverlayPermission();
        public boolean appPermissions(boolean requestPerms);
        public void showRationalDialog();
        public int getTJPoints();
        public void showVoluntaryAd();
    }

    private void showLowRatingPopup() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.bad_rating_title));
        alertDialogBuilder
            .setMessage(getString(R.string.bad_rating_message))
            .setPositiveButton(getString(R.string.bad_rating_email), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    FeedbackUtils.askForFeedback(getActivity());
                }
            })
            .setNegativeButton(getString(R.string.bad_rating_rate), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    FeedbackUtils.jumpToStore(getActivity(), preferences);
                }
            });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    /*******************private void showFeedbackPopup() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.feedback_title));
        alertDialogBuilder
                .setMessage(getString(R.string.feedback))
                .setPositiveButton(getString(R.string.feedback_email), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FeedbackUtils.askForFeedback(getActivity());
                    }
                })
                .setNegativeButton(getString(R.string.feedback_rate), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        jumpToStore();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }********************/

    private void shareApp() {
        PackageManager pm = getActivity().getPackageManager();
        String installer = pm.getInstallerPackageName(getActivity().getApplicationContext().getPackageName());

        String appLink = "https://play.google.com/store/apps/details?id=" + getString(R.string.pref_namespace);
        if(installer != null && !installer.equals("") && installer.contains("amazon")) appLink = "http://www.amazon.com/gp/mas/dl/android?p=" + getString(R.string.pref_namespace);
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String sAux = "\nI love this android app!\n\n";
            sAux += appLink + " \n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Choose One"));
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Unable to share app link. Please send a bug report through the feedback link in the settings. Thanks!", Toast.LENGTH_LONG);
        }
    }

    private void shareImage(String message, Uri uri, String extension) {
        PackageManager pm = getActivity().getPackageManager();
        String installer = pm.getInstallerPackageName(getActivity().getApplicationContext().getPackageName());

        String appLink = "https://play.google.com/store/apps/details?id=" + getString(R.string.pref_namespace);
        if(installer != null && !installer.equals("") && installer.contains("amazon")) appLink = "http://www.amazon.com/gp/mas/dl/android?p=" + getString(R.string.pref_namespace);

        try {
            if(extension.equals("jpg")) extension = "jpeg";
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/"+extension);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, message + "\n\n" + appLink);
            startActivity(Intent.createChooser(intent, "Choose One"));
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Unable to share image. Please send a bug report through the feedback link in the settings. Thanks!", Toast.LENGTH_LONG);
        }
    }

    /*
    private void shareImageDialog() {
        String[] extensions = { ".png", "jpg", ".bmp", ".webp", ".gif"};
        FileDialog fd = new FileDialog(getActivity(), "/", extensions, new FileDialog.FileDialogListener() {
            @Override
            public void fileDialogOutput(String path, String name, String ext) {
                File file = new File(path+"/"+name);
                Uri uri = Uri.fromFile(file);
                shareImage("Here's an awesome image I used for my Borders app!",uri,ext);
            }
        });
        fd.show();
    }
    */

    private void shareScreenshot() {
        listener.startScreenshotWorldService();
    }

    public void onScreenshotReady() {
        /** this is called when the service is ready for a screenshot **/
        Log.d("testing", "works!");
        //setup directory
        File screenshotDir = Environment.getExternalStorageDirectory();
        File temp = new File(screenshotDir.getAbsolutePath() + "/Borders");
        if(!temp.exists() || !temp.isDirectory())
            temp.mkdir();
        File temp2 = new File(temp + "/Screenshots");
        if(!temp2.exists() || !temp2.isDirectory())
            temp2.mkdir();
        screenshotDir = temp2;


        //setup default view
        View v = getActivity().getWindow().getDecorView().getRootView();
        if(WorldService.runningInstance != null)
            v = WorldService.runningInstance.serviceView.getRootView();

        //setup vars and file name
        v.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_hh:mm:ss");
        String date = simpleDateFormat.format(new Date());
        OutputStream out = null;
        File imageFile = new File(screenshotDir.getAbsolutePath() + "/Borders_" + date + ".png");

        //create image
        try {
            out = new FileOutputStream(imageFile);
            // choose JPEG format
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        } catch (Exception e) {
            Toast.makeText(getActivity(),"Unable to take screenshot. Please send a bug report through the feedback link in the settings. Thanks!",Toast.LENGTH_LONG);
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception exc) {
                Toast.makeText(getActivity(),"Unable to take screenshot. Please send a bug report through the feedback link in the settings. Thanks!",Toast.LENGTH_LONG);
            }
        }

        listener.stopScreenshotWorldService();

        //share new image
        shareImage("Check out my border on the Borders app!", Uri.fromFile(imageFile),"png");
    }

    public void showSharePopup() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.share_title));
        alertDialogBuilder
            .setMessage(getString(R.string.share_message))
            .setNegativeButton(getString(R.string.share_app), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    shareApp();
                }
            })
            .setPositiveButton(getString(R.string.share_screenshot), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    shareScreenshot();
                }
            });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void showVoluntaryAdPopup(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.voluntary_title));
        alertDialogBuilder
                .setMessage(getString(R.string.voluntary_message)+"\n\n------------------------------------------------------------\nYour B-Points:  "+listener.getTJPoints()+"/"+getResources().getInteger(R.integer.unlock_points)+"\n------------------------------------------------------------")
                .setNegativeButton(getString(R.string.voluntary_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.voluntary_video), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        listener.showVoluntaryAd();
                    }
                });

        if(listener.getTJPoints() >= 750) {
            alertDialogBuilder.setNeutralButton(getString(R.string.voluntary_unlock), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    /////////////////////////////////////////////////////////////////////////////unlock it manually
                }
            });
        }

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (HomeFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement HomeFragmentListener");
        }
    }

    public void setServiceToggle() {
        Log.d("service","setservicetoggle");
        Log.d("prefs","homef ft: "+Boolean.toString(preferences.getBoolean(getString(R.string.first_time_pref),true)));
        boolean firstTimer = preferences.getBoolean(getResources().getString(R.string.first_time_pref),true);
        if(firstTimer) {
            //toggleSwitch.setChecked(true);
            listener.firstTimer();
        } else {
            boolean currServiceValue = preferences.getBoolean(getResources().getString(R.string.service_toggled_pref), false);
            toggleSwitch.setChecked(currServiceValue);
        }
    }

    private void setServiceToggle(boolean toggled) {
        Log.d("service","setservicetoggle bool");
        preferences.edit().putBoolean(getString(R.string.service_toggled_pref), toggled).commit();
        if(toggled)
            listener.startWorldService(false,"");
        else
            listener.stopWorldService();
    }

    public void setToggleWithoutService(boolean toggled) {
        Log.d("service","settoggleWOservice");
        toggleSwitch.setOnCheckedChangeListener(null);
        toggleSwitch.setChecked(toggled);
        toggleSwitch.setOnCheckedChangeListener(checkedChangeListener);
        preferences.edit().putBoolean(getString(R.string.service_toggled_pref), toggled).commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_home, container, false);

        mainEditButton = (ImageButton)mainView.findViewById(R.id.mainEditButton);
        toggleSwitch = (Switch)mainView.findViewById(R.id.serviceSwitch);
        preferences = getActivity().getSharedPreferences(getString(R.string.pref_namespace), getActivity().MODE_PRIVATE);
        ratingCont = (RelativeLayout)mainView.findViewById(R.id.rating_container);
        rank1 = (ImageView)mainView.findViewById(R.id.rating_1_star);
        rank2 = (ImageView)mainView.findViewById(R.id.rating_2_star);
        rank3 = (ImageView)mainView.findViewById(R.id.rating_3_star);
        rank4 = (ImageView)mainView.findViewById(R.id.rating_4_star);
        rank5 = (ImageView)mainView.findViewById(R.id.rating_5_star);
        ///////////////////feedbackBtn = (Button)mainView.findViewById(R.id.feedbackBtn);
        settingsBtn = (Button)mainView.findViewById(R.id.settingsBtn);
        shareBtn = (Button)mainView.findViewById(R.id.shareBtn);
        pointsBtn = (Button)mainView.findViewById(R.id.pointsBtn);

        bad_rating = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLowRatingPopup();
            }
        };
        good_rating = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedbackUtils.jumpToStore(getActivity(),preferences);
            }
        };

        mainEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener.appPermissions(false))
                    listener.showEditScreen(v.getX()+v.getWidth()/2,v.getY()+v.getHeight()/2);
                else
                    listener.showRationalDialog();
            }
        });

        checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Log.d("service","check changed");
                if(listener.appPermissions(false)) {
                    setServiceToggle(isChecked);
                } else {
                    setServiceToggle(false);
                    listener.showRationalDialog();
                }
            }
        };

        toggleSwitch.setOnCheckedChangeListener(checkedChangeListener);

        rank1.setOnClickListener(bad_rating);
        rank2.setOnClickListener(bad_rating);
        rank3.setOnClickListener(good_rating);
        rank4.setOnClickListener(good_rating);
        rank5.setOnClickListener(good_rating);

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener.appPermissions(false))
                    listener.onShowSharePopup();
                else
                    listener.showRationalDialog();
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener.appPermissions(false))
                    listener.showSettings();
                else
                    listener.showRationalDialog();
            }
        });

        pointsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVoluntaryAdPopup();
            }
        });

        /************feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFeedbackPopup();
            }
        });************/

        return mainView;
    }

    @Override
    public void onStart() {
        if(preferences.getBoolean(getString(R.string.rate_us_pref),false))
            ratingCont.setVisibility(View.GONE);
        if(preferences.getBoolean(getString(R.string.unlocked_pref),false))
            pointsBtn.setVisibility(View.GONE);

        super.onStart();
    }

    @Override
    public void onResume() {
        if(preferences.getBoolean(getString(R.string.rate_us_pref),false))
            ratingCont.setVisibility(View.GONE);
        if(preferences.getBoolean(getString(R.string.unlocked_pref),false))
            pointsBtn.setVisibility(View.GONE);

        Log.d("service","home onresume");
        setServiceToggle();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
