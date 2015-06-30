package com.stuartrosk.littleworlds.app;


import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private ImageButton mainEditButton;
    private Switch toggleSwitch;
    private SharedPreferences preferences;
    private HomeFragmentListener listener;

    public HomeFragment() {
        // Required empty public constructor
    }

    public interface HomeFragmentListener {
        public void startWorldService(boolean editMode, String editPos);
        public void stopWorldService();
        public void showEditScreen();
        public boolean isServiceRunning();
        public void firstTimer();
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
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mainEditButton = (ImageButton)v.findViewById(R.id.mainEditButton);
        toggleSwitch = (Switch)v.findViewById(R.id.serviceSwitch);
        preferences = getActivity().getPreferences(getActivity().MODE_PRIVATE);

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

        return v;
    }

    @Override
    public void onStart() {
        setServiceToggle();
        super.onStart();
    }

    @Override
    public void onResume() {
        setServiceToggle();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
