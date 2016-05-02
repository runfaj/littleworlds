package com.stuartrosk.borders;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;


public class UnlockDialog extends DialogPreference {

    public UnlockDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public void showDialog() {
        /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        onPrepareDialogBuilder(alertDialogBuilder);
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();*/

        Log.d("store","showDialog");
        MainActivity.storeJump(getContext(),getContext().getString(R.string.paid_app_name));
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        super.onPrepareDialogBuilder(builder);

        builder
            .setTitle(getContext().getString(R.string.unlock_popup_title))
            .setMessage(getContext().getString(R.string.unlock_popup_message))
            .setPositiveButton(getContext().getString(R.string.unlock_popup_go), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //boolean methodAvailable = true;
                    try {
                    //////////////////    ((MainActivity) getContext()).processUnlock();
                        MainActivity.storeJump(getContext(),getContext().getString(R.string.paid_app_name));
                    } catch (NoSuchMethodError e) {
                        /*methodAvailable = false;
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.putExtra("process_unlock_app", true);
                        getContext().startActivity(intent);*/
                    }
                }
            })
            .setNegativeButton(getContext().getString(R.string.unlock_popup_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
    }
}
