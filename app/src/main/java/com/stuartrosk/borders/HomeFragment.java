package com.stuartrosk.borders;


import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;



/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends SuperFragment {

    private SwitchCompat toggleSwitch;
    private SharedPreferences preferences;
    private HomeFragmentListener listener;
    private AppCompatImageView pointsBtn;
    private RelativeLayout ratingCont;
    private CompoundButton.OnCheckedChangeListener checkedChangeListener = null;

    public HomeFragment() {
        // Required empty public constructor
    }

    public interface HomeFragmentListener {
        void startWorldService(boolean editMode, String editPos);
        void stopWorldService();
        void showEditScreen(float x, float y);
        boolean isServiceRunning();
        void firstTimer();
        void startScreenshotWorldService();
        void stopScreenshotWorldService();
        void showSettings();
        void onShowSharePopup();
        //boolean requestOverlayPermission();
        boolean appPermissions(boolean requestPerms);
        void showRationalDialog();
        int getAdPoints();
        void showVoluntaryAd();
        void shareApp();
        void adUnlock();
    }

    private void showLowRatingPopup() {
        sendEvent("Popup","Rate","From Home");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.bad_rating_title));
        alertDialogBuilder
            .setMessage(getString(R.string.bad_rating_message))
            .setPositiveButton(getString(R.string.bad_rating_email), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    sendEvent("Rate","Lower Score","Email");
                    FeedbackUtils.askForFeedback(getActivity());
                }
            })
            .setNegativeButton(getString(R.string.bad_rating_rate), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    sendEvent("Rate","Lower Score","Continue Rating");
                    FeedbackUtils.jumpToStore(getActivity(), preferences);
                }
            });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void shareScreenshot() {
        listener.startScreenshotWorldService();
    }

    public void showSharePopup() {
        sendEvent("Popup","Share","From Home");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.share_title));
        alertDialogBuilder
            .setMessage(getString(R.string.share_message))
            .setNegativeButton(getString(R.string.share_app), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    sendEvent("Share","App","From Home");
                    listener.shareApp();
                }
            })
            .setPositiveButton(getString(R.string.share_screenshot), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    sendEvent("Share","Screenshot","From Home");
                    shareScreenshot();
                }
            });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void showVoluntaryAdPopup(){
        sendEvent("Popup","B-Points","From Home");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.voluntary_title));

        int actualPoints = preferences.getInt(getString(R.string.points_override),
                getResources().getInteger(R.integer.unlock_points));

        alertDialogBuilder
                .setMessage(getString(R.string.voluntary_message)+"\n\n------------------------------------------------------------\nYour B-Points:  "+listener.getAdPoints()+"/"+actualPoints+"\n------------------------------------------------------------")
                /*.setNegativeButton(getString(R.string.voluntary_offer), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        sendEvent("Voluntary Ad","Offerwall");
                        listener.showOfferwall();
                    }
                })*/
                .setPositiveButton(/*getString(R.string.voluntary_video)*/"Sounds Good", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        //sendEvent("Voluntary Ad","Show Ad");
                        //listener.showVoluntaryAd();
                    }
                });

        if(listener.getAdPoints() >= actualPoints) {
            alertDialogBuilder.setNeutralButton(getString(R.string.voluntary_unlock), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    sendEvent("Voluntary Ad","Unlock");
                    listener.adUnlock();
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
        View mainView = inflater.inflate(R.layout.fragment_home, container, false);

        FloatingActionButton mainEditButton = (FloatingActionButton) mainView.findViewById(R.id.mainEditButton);
        toggleSwitch = (SwitchCompat) mainView.findViewById(R.id.serviceSwitch);
        preferences = getActivity().getSharedPreferences(getString(R.string.pref_namespace), getActivity().MODE_PRIVATE);
        ratingCont = (RelativeLayout) mainView.findViewById(R.id.rating_container);
        AppCompatImageView rank1 = (AppCompatImageView) mainView.findViewById(R.id.rating_1_star);
        AppCompatImageView rank2 = (AppCompatImageView) mainView.findViewById(R.id.rating_2_star);
        AppCompatImageView rank3 = (AppCompatImageView) mainView.findViewById(R.id.rating_3_star);
        AppCompatImageView rank4 = (AppCompatImageView) mainView.findViewById(R.id.rating_4_star);
        AppCompatImageView rank5 = (AppCompatImageView) mainView.findViewById(R.id.rating_5_star);
        ///////////////////feedbackBtn = (Button)mainView.findViewById(R.id.feedbackBtn);
        AppCompatImageView settingsBtn = (AppCompatImageView) mainView.findViewById(R.id.settingsBtn);
        AppCompatImageView shareBtn = (AppCompatImageView) mainView.findViewById(R.id.shareBtn);
        AppCompatButton themesBtn = (AppCompatButton) mainView.findViewById(R.id.themesBtn);
        pointsBtn = (AppCompatImageView) mainView.findViewById(R.id.pointsBtn);

        View.OnClickListener bad_rating = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLowRatingPopup();
            }
        };
        View.OnClickListener good_rating = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEvent("Rate","Higher Score","");
                FeedbackUtils.jumpToStore(getActivity(), preferences);
            }
        };

        themesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEvent("Community","Themes Page","From Home");
                FeedbackUtils.openGooglePlusPage(getActivity());
            }
        });

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
                sendEvent("Toggle","Service","From Home");
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
                sendEvent("Popup","Settings");
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

        sendView(getClass().getSimpleName());

        return mainView;
    }

    public void hidePointsButton(){
        if(preferences.getBoolean(getString(R.string.unlocked_pref),false))
            pointsBtn.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        if(preferences.getBoolean(getString(R.string.rate_us_pref),false))
            ratingCont.setVisibility(View.GONE);
        hidePointsButton();

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
