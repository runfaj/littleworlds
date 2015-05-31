package com.stuartrosk.littleworlds.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.ListAdapter;

public class ImageListPreference extends ListPreference
{
    private String[] resourceNames = null;
    private TypedArray resourceImages = null;
    private int[] resourceValues = null;
    SharedPreferences preferences;

    public ImageListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        resourceNames = context.getResources().getStringArray(R.array.imageListNames);
        resourceImages = context.getResources().obtainTypedArray(R.array.imageListImages);
        resourceValues = context.getResources().getIntArray(R.array.imageListValues);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        int index = preferences.getInt("theme",1);

        System.out.println(index);

        ListAdapter listAdapter = new ImageListAdapter(getContext(),
                R.layout.image_list_row, this.getEntries(),
                resourceValues, resourceNames, resourceImages, index,
                this);

        builder.setAdapter(listAdapter, this);
        super.onPrepareDialogBuilder(builder);
    }

    public void setResult(int clicked)
    {
        if(this.callChangeListener(""+clicked))
        {
            System.out.println("Sel: "+clicked);
            preferences.edit().putInt("theme", clicked).commit();
        }
        this.getDialog().dismiss();
    }
}