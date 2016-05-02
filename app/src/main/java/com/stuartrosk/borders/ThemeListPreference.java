package com.stuartrosk.borders;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;


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
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Log.d("index",Integer.toString(findIndexOfValue(getValue())));
        ((ListView)view).smoothScrollToPosition(findIndexOfValue(getValue()));
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        int index = preferences.getInt(getContext().getString(R.string.theme_id),getContext().getResources().getInteger(R.integer.default_theme_id));

        ListAdapter listAdapter = new ThemeListAdapter(getContext(),
                R.layout.image_list_row, this.getEntries(),
                themes, index,
                this);

        //builder.setAdapter(listAdapter, this);
        builder.setSingleChoiceItems(listAdapter, index, this);

        super.onPrepareDialogBuilder(builder);
    }

    public void setResult(int clicked)
    {
        preferences.edit()
                .putInt(getContext().getString(R.string.prev_theme_id), preferences.getInt(getContext().getString(R.string.theme_id), getContext().getResources().getInteger(R.integer.default_theme_id)))
                .putString(getContext().getString(R.string.prev_theme_key), preferences.getString(getContext().getString(R.string.theme_key), getContext().getString(R.string.default_theme_key)))
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