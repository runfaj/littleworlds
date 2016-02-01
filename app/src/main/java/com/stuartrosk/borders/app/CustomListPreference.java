package com.stuartrosk.borders.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.ListAdapter;

public class CustomListPreference extends ListPreference
{
    ThemeJsonObject.Theme[] themes;
    SharedPreferences preferences;

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        preferences = context.getSharedPreferences(context.getString(R.string.pref_namespace), context.MODE_PRIVATE);
        themes = ThemeJsonObject.getCustomThemes(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        int index = preferences.getInt(getContext().getString(R.string.theme_id),getContext().getResources().getInteger(R.integer.default_theme_id));

        System.out.println(index);

        ListAdapter listAdapter = new CustomListAdapter(getContext(),
                R.layout.image_list_row, this.getEntries(),
                themes, index,
                this);

        builder.setAdapter(listAdapter, this);
        super.onPrepareDialogBuilder(builder);
    }

    public void setResult(int clickedID)
    {
        themes = ThemeJsonObject.getCustomThemes(getContext());
        ThemeJsonObject.Theme selectedTheme = themes[0];
        for(ThemeJsonObject.Theme theme : themes) {
            if(theme.id == clickedID)
                selectedTheme = theme;
        }

        preferences.edit()
                .putInt(getContext().getString(R.string.custom_id), clickedID)
                .putString(getContext().getString(R.string.custom_key), selectedTheme.title)
                .commit();

        this.callChangeListener(""+clickedID);

        hideDialog();
    }

    public void hideDialog() {
        if(this.getDialog()!=null)
            this.getDialog().dismiss();
    }
}