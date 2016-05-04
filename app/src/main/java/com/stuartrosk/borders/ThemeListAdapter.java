package com.stuartrosk.borders;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;


public class ThemeListAdapter extends ArrayAdapter<CharSequence> implements View.OnClickListener {
    int selected = 0;
    ThemeJsonObject.Theme[] themes;

    private ThemeListPreference ts;
    private SharedPreferences preferences;

    public ThemeListAdapter(Context context, int textViewResourceId,
                            CharSequence[] objects, ThemeJsonObject.Theme[] themes, int i, ThemeListPreference ts) {
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
        AppCompatTextView tv = (AppCompatTextView) row.findViewById(R.id.themeName);
        tv.setText(themes[position].title);

        //set image
        final AppCompatImageView ti = (AppCompatImageView) row.findViewById(R.id.themeImage);
        try {
            BitmapDrawable b = ((BitmapDrawable)ti.getDrawable());
            if(b!=null) b.getBitmap().recycle();

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
        AppCompatRadioButton tb = (AppCompatRadioButton) row.findViewById(R.id.ckbox);
        AppCompatImageView iv = (AppCompatImageView) row.findViewById(R.id.lockedIcon);
        if((preferences.getBoolean(getContext().getString(R.string.unlocked_pref),false) && themes[position].paid_content)
                || !themes[position].paid_content) {
            if (themes[position].id == selected) {
                tb.setChecked(true);
            } else {
                tb.setChecked(false);
            }
            tb.setClickable(false);
            iv.setVisibility(View.GONE);

            //set on click listener for row
            row.setOnClickListener(this);
        } else {
            tb.setVisibility(View.GONE);
            iv.setVisibility(View.VISIBLE);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ts.hideDialog();

                    UnlockDialog unlockDialog = new UnlockDialog(getContext(),null);
                    unlockDialog.showDialog();
                }
            });
        }

        return row;
    }

    @Override
    public void onClick(View v)
    {
        ts.setResult(v.getId());
    }
}