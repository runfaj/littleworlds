package com.stuartrosk.littleworlds.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.ListAdapter;

public class EditListPreference extends ListPreference
{
    private String[] resourceNames = null;
    private String[] resourceImages = null;
    private String[] resourceValues = null;
    SharedPreferences preferences;

    public EditListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        preferences = context.getSharedPreferences("com.stuartrosk.littleworlds", context.MODE_PRIVATE);

        ThemeJsonObject.Theme[] themes = ThemeJsonObject.getThemes(context);
        resourceNames = new String[themes.length];
        resourceImages = new String[themes.length];
        resourceValues = new String[themes.length];

        for(int i=0;i<themes.length;++i) {
            resourceNames[i] = themes[i].title;
            resourceImages[i] = themes[i].theme_image_name;
            resourceValues[i] = String.valueOf(themes[i].id);
        }

        //resourceNames = context.getResources().getStringArray(R.array.imageListNames);
        //resourceImages = context.getResources().obtainTypedArray(R.array.imageListImages);
        //resourceValues = context.getResources().getStringArray(R.array.imageListValues);
    }



    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        int index = preferences.getInt(getContext().getString(R.string.theme_id),1);

        System.out.println(index);

        ListAdapter listAdapter = new ThemeListAdapter(getContext(),
                R.layout.image_list_row, this.getEntries(),
                resourceValues, resourceNames, resourceImages, index,
                this);

        builder.setAdapter(listAdapter, this);
        super.onPrepareDialogBuilder(builder);
    }

    public void setResult(int clicked)
    {
        System.out.println("Sel: "+clicked);
        System.out.println("SelName: "+resourceNames[clicked-1]);

        preferences.edit()
                .putInt(getContext().getString(R.string.theme_id), clicked)
                .putString(getContext().getString(R.string.theme_key), resourceNames[clicked-1])
        .commit();

        this.callChangeListener(""+clicked);

        if(this.getDialog()!=null)
            this.getDialog().dismiss();
    }
}