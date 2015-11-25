package com.stuartrosk.borders.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.ListAdapter;

public class ThemeListPreference extends ListPreference
{
    ThemeJsonObject.Theme[] themes;
    SharedPreferences preferences;

    public ThemeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        preferences = context.getSharedPreferences(context.getString(R.string.pref_namespace), context.MODE_PRIVATE);
        themes = ThemeJsonObject.getThemes(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        int index = preferences.getInt(getContext().getString(R.string.theme_id),1);

        System.out.println(index);

        ListAdapter listAdapter = new ThemeListAdapter(getContext(),
                R.layout.image_list_row, this.getEntries(),
                themes, index,
                this);

        builder.setAdapter(listAdapter, this);
        super.onPrepareDialogBuilder(builder);
    }

    public void setResult(int clicked)
    {
        preferences.edit()
                .putInt(getContext().getString(R.string.theme_id), clicked)
                .putString(getContext().getString(R.string.theme_key), themes[clicked-1].title)
        .commit();

        this.callChangeListener(""+clicked);

        hideDialog();
    }

    public void hideDialog() {
        if(this.getDialog()!=null)
            this.getDialog().dismiss();
    }
}