package com.stuartrosk.borders.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class FeedbackDialog extends DialogPreference {

    private SharedPreferences preferences;

    public FeedbackDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        super.onPrepareDialogBuilder(builder);

        preferences = getContext().getSharedPreferences(getContext().getString(R.string.pref_namespace), getContext().MODE_PRIVATE);

        builder
            .setTitle(getContext().getString(R.string.feedback_title))
            .setMessage(getContext().getString(R.string.feedback_message))
            .setPositiveButton(getContext().getString(R.string.feedback_email), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    FeedbackUtils.askForFeedback(getContext());
                }
            })
            .setNegativeButton(getContext().getString(R.string.feedback_rate), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    FeedbackUtils.jumpToStore(getContext(), preferences);
                }
            });
    }
}
