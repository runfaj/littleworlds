package com.stuartrosk.borders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;


public class FeedbackDialog extends DialogPreference {

    private SharedPreferences preferences;

    public FeedbackDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        MainActivity.sendEvent(getContext(),"Popup","Feedback","From Settings",null);

        super.onPrepareDialogBuilder(builder);

        preferences = getContext().getSharedPreferences(getContext().getString(R.string.pref_namespace), getContext().MODE_PRIVATE);

        builder
            .setTitle(getContext().getString(R.string.feedback_title))
            .setMessage(getContext().getString(R.string.feedback_message))
            .setPositiveButton(getContext().getString(R.string.feedback_email), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    MainActivity.sendEvent(getContext(),"Popup","Email Us","From Settings",null);
                    FeedbackUtils.askForFeedback(getContext());
                }
            })
            .setNegativeButton(getContext().getString(R.string.feedback_rate), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    MainActivity.sendEvent(getContext(),"Popup","Rate","From Settings",null);
                    FeedbackUtils.jumpToStore(getContext(), preferences);
                }
            });
    }
}
