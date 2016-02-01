package com.stuartrosk.borders.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

public class CustomListAdapter extends ArrayAdapter<CharSequence> implements View.OnClickListener {
    int selected = 0;
    ThemeJsonObject.Theme[] themes;

    private CustomListPreference ts;
    private SharedPreferences preferences;

    public CustomListAdapter(Context context, int textViewResourceId,
                            CharSequence[] objects, ThemeJsonObject.Theme[] themes, int i, CustomListPreference ts) {
        super(context, textViewResourceId, objects);

        selected = i;
        this.themes = themes;
        this.ts = ts;
        preferences = context.getSharedPreferences(context.getString(R.string.pref_namespace),context.MODE_PRIVATE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //inflate layout
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        View row = inflater.inflate(R.layout.image_list_row, parent, false);

        //set id
        row.setId(themes[position].id);

        //set name
        TextView tv = (TextView) row.findViewById(R.id.themeName);
        tv.setText(themes[position].title);

        //set image
        final ImageView ti = (ImageView) row.findViewById(R.id.themeImage);
        try {
            ti.setImageDrawable(
                Drawable.createFromStream(
                    getContext().getAssets().open(ThemeJsonObject.getPreviewFile(themes[position])),
                    null
                )
            );
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
        //ti.setImageResource(resourceImages.getResourceId(position,-1));

        //set checkbox
        RadioButton tb = (RadioButton) row.findViewById(R.id.ckbox);
        ImageView iv = (ImageView) row.findViewById(R.id.lockedIcon);
            if (themes[position].id == selected) {
                tb.setChecked(true);
            } else {
                tb.setChecked(false);
            }
            tb.setClickable(false);
            iv.setVisibility(View.GONE);

            //set on click listener for row
            row.setOnClickListener(this);

        return row;
    }

    @Override
    public void onClick(View v)
    {
        ts.setResult(v.getId());
    }
}