package com.stuartrosk.borders.app;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
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
    private Button shareBtn, settingsBtn;
    private View mainView;
    private RelativeLayout ratingCont;

    public HomeFragment() {
        // Required empty public constructor
    }

    public interface HomeFragmentListener {
        public void startWorldService(boolean editMode, String editPos);
        public void stopWorldService();
        public void showEditScreen();
        public boolean isServiceRunning();
        public void firstTimer();
        public void startScreenshotWorldService();
        public void stopScreenshotWorldService();
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
                    jumpToStore();
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

    private void shareImage(String message, Uri uri) {
        PackageManager pm = getActivity().getPackageManager();
        String installer = pm.getInstallerPackageName(getActivity().getApplicationContext().getPackageName());

        String appLink = "https://play.google.com/store/apps/details?id=" + getString(R.string.pref_namespace);
        if(installer != null && !installer.equals("") && installer.contains("amazon")) appLink = "http://www.amazon.com/gp/mas/dl/android?p=" + getString(R.string.pref_namespace);

        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, message + "\n\n" + appLink);
            startActivity(Intent.createChooser(intent, "Choose One"));
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Unable to share image. Please send a bug report through the feedback link in the settings. Thanks!", Toast.LENGTH_LONG);
        }
    }

    private void shareImageDialog() {
        String[] extensions = { ".png", "jpg", ".bmp", ".webp", ".gif"};
        FileDialog fd = new FileDialog(getActivity(), "/", extensions, new FileDialog.FileDialogListener() {
            @Override
            public void fileDialogOutput(String path, String name) {
                File file = new File(path+"/"+name);
                Uri uri = Uri.fromFile(file);
                shareImage("Here's an awesome image I used for my Borders app!",uri);
            }
        });
        fd.show();
    }

    private void shareScreenshot() {
        listener.startScreenshotWorldService();
    }
    public void onScreenshotReady() {
        /** this is called when the service is ready for a screenshot **/
        Log.d("testing", "works!");
        //setup directory
        File screenshotDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File temp = new File(screenshotDir.getAbsolutePath() + "/Borders_Screenshots");
        if(!temp.exists() || !temp.isDirectory())
            temp.mkdir();
        screenshotDir = temp;


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
        shareImage("Check out my border on the Borders app!", Uri.fromFile(imageFile));
    }

    private void showSharePopup() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.share_title));
        alertDialogBuilder
            .setMessage(getString(R.string.share_message))
            .setNegativeButton(getString(R.string.share_app), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    shareApp();
                }
            })
            .setNeutralButton(getString(R.string.share_image), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    shareImageDialog();
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

    private void jumpToStore() {
        PackageManager pm = getActivity().getPackageManager();
        String installer = pm.getInstallerPackageName(getActivity().getApplicationContext().getPackageName());
        if(installer == null) installer = "";
        FeedbackUtils.openApp(getActivity(),installer);
        preferences.edit().putBoolean(getString(R.string.rate_us_pref),true).commit();
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
                    + " must implement EditFragmentListener");
        }
    }

    private void setServiceToggle() {
        boolean firstTimer = preferences.getBoolean(getResources().getString(R.string.first_time_pref),true);
        if(firstTimer) {
            toggleSwitch.setChecked(true);
            listener.firstTimer();
        } else {
            boolean currServiceValue = preferences.getBoolean(getResources().getString(R.string.service_enabled_pref), false);
            preferences.edit().putBoolean(getString(R.string.service_enabled_pref), currServiceValue).commit();
            toggleSwitch.setChecked(currServiceValue);
            if(currServiceValue)
                listener.startWorldService(false,"");
            else
                listener.stopWorldService();
        }
    }

    private void setServiceToggle(boolean toggled) {
        preferences.edit().putBoolean(getString(R.string.service_enabled_pref), toggled).commit();
        if(toggled)
            listener.startWorldService(false,"");
        else
            listener.stopWorldService();
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

        bad_rating = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLowRatingPopup();
            }
        };
        good_rating = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToStore();
            }
        };

        mainEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.showEditScreen();
            }
        });

        toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                setServiceToggle(isChecked);
            }
        });

        rank1.setOnClickListener(bad_rating);
        rank2.setOnClickListener(bad_rating);
        rank3.setOnClickListener(good_rating);
        rank4.setOnClickListener(good_rating);
        rank5.setOnClickListener(good_rating);

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSharePopup();
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
        setServiceToggle();
        if(preferences.getBoolean(getString(R.string.rate_us_pref),false))
            ratingCont.setVisibility(View.INVISIBLE);
        super.onStart();
    }

    @Override
    public void onResume() {
        setServiceToggle();
        if(preferences.getBoolean(getString(R.string.rate_us_pref),false))
            ratingCont.setVisibility(View.INVISIBLE);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
