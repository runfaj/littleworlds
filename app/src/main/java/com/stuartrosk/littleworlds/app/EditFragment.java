package com.stuartrosk.littleworlds.app;


import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditFragment extends Fragment {

    private SharedPreferences preferences;
    private Button editDoneBtn;
    private EditFragmentListener listener;
    private PreferenceListFragment preferenceListFragment;

    public EditFragment() {
        // Required empty public constructor
    }

    public interface EditFragmentListener {
        public void onFinishEdit();
        public void onStartEdit();
        public void hideEditScreen();
        public void showImageEditScreen(String editPos);
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
        View v = inflater.inflate(R.layout.fragment_edit, container, false);

        preferences = getActivity().getPreferences(getActivity().MODE_PRIVATE);
        editDoneBtn = (Button)v.findViewById(R.id.editDoneBtn);
        preferenceListFragment = new PreferenceListFragment();

        //init fragment
        getFragmentManager().beginTransaction()
            .replace(R.id.myPrefFragmentCont, preferenceListFragment)
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
            if(vg.getChildAt(i) instanceof ImageButton) {
                vg.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String btnName = v.getTag().toString();
                        listener.showImageEditScreen(btnName);
                    }
                });
            }
        }

        preferences.edit().putBoolean(getString(R.string.edit_mode_pref),false).commit();

        return v;
    }

    public void updatePreferenceList(int index) {
        if(preferenceListFragment != null) {
            preferences.edit()
                .putString(getString(R.string.theme_id), ""+index)
                .putString(getString(R.string.theme_key), "Custom")
            .commit();
            preferenceListFragment.updateThemeEntry(index);
        }
    }

    @Override
    public void onDestroy() {
        //if we were in edit mode, cancel it
        if(preferences.getBoolean(getString(R.string.edit_mode_pref),false))
            listener.onFinishEdit();

        super.onDestroy();
    }
    @Override
    public void onPause() {
        //if we are supposed to keep service, restart without the edit controls
        if(preferences.getBoolean(getString(R.string.edit_mode_pref),false)) {
            listener.onFinishEdit();
        }

        super.onPause();
    }
    @Override
    public void onResume() {
        if(preferences.getBoolean(getString(R.string.edit_mode_pref),false)) {
            listener.onStartEdit();
        }

        super.onResume();
    }
}
