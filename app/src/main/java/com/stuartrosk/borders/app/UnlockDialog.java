package com.stuartrosk.borders.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

public class UnlockDialog extends DialogPreference {

    private SharedPreferences preferences;

    public UnlockDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public void showDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        onPrepareDialogBuilder(alertDialogBuilder);
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        super.onPrepareDialogBuilder(builder);

        preferences = getContext().getSharedPreferences(getContext().getString(R.string.pref_namespace), getContext().MODE_PRIVATE);

        builder
            .setTitle(getContext().getString(R.string.unlock_popup_title))
            .setMessage(getContext().getString(R.string.unlock_popup_message))
            .setPositiveButton(getContext().getString(R.string.unlock_popup_go), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    boolean methodAvailable = true;
                    try {
                        ((MainActivity) getContext()).processUnlock();
                    } catch (NoSuchMethodError e) {
                        methodAvailable = false;
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.putExtra("process_unlock_app", true);
                        getContext().startActivity(intent);
                    }
                    Log.d("IAPservice","Method Available in Unlock Dialog: " + methodAvailable);
                }
            })
            .setNegativeButton(getContext().getString(R.string.unlock_popup_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
    }
}
