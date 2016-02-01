package com.stuartrosk.borders.app;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomFragment extends Fragment {

    View v;
    private CustomFragmentListener listener;
    private ImageView saveButton, exportButton, deleteButton;
    private SharedPreferences preferences;
    private int curr_id;

    public CustomFragment() {
        // Required empty public constructor
    }

    public interface CustomFragmentListener {
        public void showImageEditScreen(String editPos);
        public boolean appPermissions(boolean requestPerms);
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

        curr_id = preferences.getInt(getString(R.string.custom_id),1);

        saveButton = (ImageView) v.findViewById(R.id.custom_save_btn); ///////////////////////////////////////////////////////
        deleteButton = (ImageView) v.findViewById(R.id.custom_delete_btn); ///////////////////////////////////////////////////////
        exportButton = (ImageView) v.findViewById(R.id.custom_export_btn); ///////////////////////////////////////////////////////

        //set edit button handlers
        ViewGroup vg = (ViewGroup)v.findViewById(R.id.fragmentEdit);
        for(int i=0;i<vg.getChildCount();i++) {
            if(vg.getChildAt(i) instanceof ImageView) {
                testUnlocked(vg.getChildAt(i));
            }
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
                ///////////////////////////////////////////////////////are you sure? y=delete
            }
        });

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportCustomTheme();
            }
        });

        return v;
    }

    public void saveTheme(){
        ///////////////////////////////////////////////////////////////////////////////// TODO
    }

    public void deleteTheme(){
        ///////////////////////////////////////////////////////////////////////////////// TODO
    }

    public void exportCustomTheme(){
        ///////////////////////////////////////////////////////////////////////////////// TODO
    }

    public void onBackPressed(){
        ///////////////////////////////////////////////////////////////////////////////// TODO
        //popup asking if they want to save or not - y/n/c popup
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
}
