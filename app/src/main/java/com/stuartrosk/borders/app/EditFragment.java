package com.stuartrosk.borders.app;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditFragment extends Fragment {

    private SharedPreferences preferences;
    private Button editDoneBtn;
    private EditFragmentListener listener;
    private EditPrefListFragment editPrefListFragment;
    private View v;

    public EditFragment() {
        // Required empty public constructor
    }

    public interface EditFragmentListener {
        public void onFinishEdit();
        public void onStartEdit();
        public void hideEditScreen();
        public void showImageEditScreen(String editPos);
    }


    public void toggleEditIcons() {
        int state;
        //if a custom theme, then show the edit buttons
        if(preferences.getInt(getString(R.string.theme_id),1) == 1)
            state = View.VISIBLE;
        else
            state = View.INVISIBLE;
        if(v==null)return;
        RelativeLayout r = (RelativeLayout)v.findViewById(R.id.fragmentEdit);
        for(int i=0;i<r.getChildCount();i++) {
            if(r.getChildAt(i) instanceof ImageView) {
                r.getChildAt(i).setVisibility(state);
            }
        }
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_edit, container, false);

        preferences = getActivity().getSharedPreferences(getString(R.string.pref_namespace), getActivity().MODE_PRIVATE);
        editDoneBtn = (Button)v.findViewById(R.id.editDoneBtn);
        editPrefListFragment = new EditPrefListFragment();

        //init fragment
        getFragmentManager().beginTransaction()
            .replace(R.id.myPrefFragmentCont, editPrefListFragment)
        .commit();

        editDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.hideEditScreen();
            }
        });

        //set edit button handlers
        ViewGroup vg = (ViewGroup)v.findViewById(R.id.fragmentEdit);
        for(int i=0;i<vg.getChildCount();i++) {
            if(vg.getChildAt(i) instanceof ImageView) {
                testUnlocked(vg.getChildAt(i));
            }
        }

        return v;
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

    public void updatePreferenceList(int index) {
        if(editPrefListFragment != null) {
            preferences.edit()
                .putInt(getString(R.string.theme_id), index)
                .putString(getString(R.string.theme_key), "Custom")
            .commit();
            editPrefListFragment.updateThemeEntry(index);
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
        listener.onStartEdit();
        toggleEditIcons();
        super.onResume();
    }
}
