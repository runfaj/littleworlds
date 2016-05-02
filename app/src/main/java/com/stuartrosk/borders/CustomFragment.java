package com.stuartrosk.borders;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomFragment extends Fragment {

    View v;
    private CustomFragmentListener listener;
    private SharedPreferences preferences;
    TextView nameField;
    ThemeJsonObject.Theme manifest;
    ImageView previewImage;

    public CustomFragment() {
        // Required empty public constructor
    }

    public interface CustomFragmentListener {
        void showImageEditScreen(String editPos);
        boolean appPermissions(boolean requestPerms);
        void hideCustomScreen();
        void onStartCustomScreen();
        void onCustomDestroy();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (CustomFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CustomFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_custom, container, false);

        preferences = getActivity().getSharedPreferences(getString(R.string.pref_namespace), getActivity().MODE_PRIVATE);

        int curr_id = preferences.getInt(getString(R.string.custom_id), 1);
        manifest = ThemeJsonObject.getCustomTheme(getActivity().getApplicationContext(), curr_id);

        ImageView saveButton = (ImageView) v.findViewById(R.id.custom_save_btn);
        ImageView deleteButton = (ImageView) v.findViewById(R.id.custom_delete_btn);
        ImageView exportButton = (ImageView) v.findViewById(R.id.custom_export_btn);
        previewImage = (ImageView) v.findViewById(R.id.preview_image);
        nameField = (TextView) v.findViewById(R.id.theme_name_tv);

        Log.d("custom create",manifest.title);
        nameField.setText(manifest.title);

        //set edit button handlers
        ViewGroup vg = (ViewGroup)v.findViewById(R.id.fragmentCustom);
        for(int i=0;i<vg.getChildCount();i++) {
            if(vg.getChildAt(i) instanceof ImageView) {
                testUnlocked(vg.getChildAt(i));
            }
        }
        ImageView previewButton = (ImageView) v.findViewById(R.id.preview_edit_btn);
        testUnlocked(previewButton);

        String fullPath = ThemeJsonObject.getCustomThemePath(manifest) + "/" + ThemeJsonObject.getPreviewFile(manifest);
        File previewFile = new File(fullPath);
        if(previewFile.exists() && !previewFile.isDirectory()) {
            previewImage.setBackgroundColor(Color.WHITE);
            try {
                BitmapDrawable b = ((BitmapDrawable) previewImage.getDrawable());
                if (b != null) b.getBitmap().recycle();
                previewImage.setImageBitmap(WorldService.decodeSampledBitmapFromFile(
                        fullPath,
                        (int)getResources().getDimension(R.dimen.previewWidth),
                        (int)getResources().getDimension(R.dimen.previewHeight)));
            } catch (Exception e) {
                if(e.getMessage() != null)
                    Log.e("error", e.getMessage());
            }
        } else {
            previewImage.setBackgroundColor(Color.rgb(239,239,239));
            try {
                BitmapDrawable b = ((BitmapDrawable) previewImage.getDrawable());
                if (b != null) b.getBitmap().recycle();
                previewImage.setImageBitmap(null);
            } catch (Exception e) {}
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTheme();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTheme();
            }
        });
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportCustomTheme();
            }
        });

        listener.onStartCustomScreen();

        return v;
    }

    public void saveTheme(){
        //hide keyboard
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        manifest.title = nameField.getText().toString();
        ThemeJsonObject.writeCustomManifest(getActivity().getApplicationContext(), manifest);
        preferences.edit().putString(getString(R.string.custom_key),manifest.title).commit();
        listener.hideCustomScreen();
    }

    public void deleteTheme(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Are you sure?");
        alertDialogBuilder
                .setMessage("Are you sure you want to delete this theme? All files associated to this theme in the Borders/Themes directory will also be removed.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(ThemeJsonObject.deleteCustomTheme(getActivity().getApplicationContext(), manifest)) {
                            Toast.makeText(getActivity().getApplicationContext(),"\""+manifest.title+"\" deleted successfully.",Toast.LENGTH_LONG).show();
                            preferences.edit()
                                .putString(getString(R.string.custom_key),"(None)")
                            .commit();
                            listener.hideCustomScreen();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(),"Unable to delete. Is the folder locked?",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void exportCustomTheme(){
        String result = ThemeJsonObject.zipCustomTheme(getActivity(), manifest);
        if(result.equals(""))
            Toast.makeText(getActivity().getApplicationContext(),"Error exporting theme. Please make sure there are no extra folders in the theme directory.",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getActivity().getApplicationContext(),"Success! Your theme can be found here: " + result,Toast.LENGTH_LONG).show();
    }

    public void onBackPressed(){
        saveTheme();
    }

    //iterable
    private void testUnlocked(View v){
        //return true if still showing the button
        String pos = v.getTag().toString();
        if(pos == null) return;

        //only giving the user 6 options if its locked
        boolean lockIt = false;
        switch(pos){
            case "top_left_corner":
            case "top_right_corner":
            case "top_left_middle":
            case "top_right_middle":
            case "side_left_middle":
            case "side_left_top":
            case "side_left_bottom":
            case "side_right_top":
                lockIt = true; break;
        }

        if(preferences.getBoolean(getString(R.string.unlocked_pref),false))
            lockIt = false;

        if(lockIt) {
            v.setRotation(0F);
            ((ImageView) v).setImageResource(R.drawable.ic_lock_outline_black_48dp);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UnlockDialog unlockDialog = new UnlockDialog(view.getContext(),null);
                    unlockDialog.showDialog();
                }
            });
        } else {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String btnName = v.getTag().toString();
                    listener.showImageEditScreen(btnName);
                }
            });

        }
    }

    @Override
    public void onDestroy() {
        listener.onCustomDestroy();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        listener.onCustomDestroy();
        super.onPause();
    }

    @Override
    public void onResume() {
        listener.onStartCustomScreen();
        Log.d("custom create",manifest.title);
        nameField.setText(manifest.title);
        super.onResume();
    }
}
