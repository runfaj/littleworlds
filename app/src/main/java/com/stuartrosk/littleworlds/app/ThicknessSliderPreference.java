package com.stuartrosk.littleworlds.app;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class ThicknessSliderPreference extends DialogPreference {
    private static int DEFAULT_VALUE = 20;
    private int currentValue = 20;
    private View v;
    private SeekBar thicknessSeekBar;
    private TextView thicknessTextView;

    @Override
    public void onBindDialogView(View v) {
        this.v = v;
        thicknessSeekBar = (SeekBar)v.findViewById(R.id.thicknessSeekBar);
        thicknessTextView = (TextView)v.findViewById(R.id.thicknessValueText);

        thicknessTextView.setText(currentValue+"");
        thicknessSeekBar.setProgress(currentValue-10);

        thicknessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                thicknessTextView.setText("" + (progress + 10));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public ThicknessSliderPreference(Context context, AttributeSet attrs) {
        super (context, attrs);
        setDialogLayoutResource(R.layout.thickness_preference);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult) {
            currentValue = ((SeekBar)v.findViewById(R.id.thicknessSeekBar)).getProgress() + 10;
            persistInt(currentValue);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            currentValue = this.getPersistedInt(DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            currentValue = DEFAULT_VALUE;
            persistInt(currentValue);
        }
        if(thicknessTextView!=null)thicknessTextView.setText(currentValue+"");
        if(thicknessSeekBar!=null)thicknessSeekBar.setProgress(currentValue-10);
    }
}
