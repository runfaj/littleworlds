package com.stuartrosk.borders;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
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


public class CustomListAdapter extends ArrayAdapter<CharSequence> implements View.OnClickListener {
    int selected = 0;
    ThemeJsonObject.Theme[] themes;
    private CustomListPreference ts;

    public CustomListAdapter(Context context, int textViewResourceId,
                            CharSequence[] objects, ThemeJsonObject.Theme[] themes, int i, CustomListPreference ts) {
        super(context, textViewResourceId, objects);

        selected = i;
        this.themes = themes;
        this.ts = ts;
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
            ti.setImageDrawable(
                Drawable.createFromStream(
                    getContext().getAssets().open(ThemeJsonObject.getPreviewFile(themes[position])),
                    null
                )
            );
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }

        try {
            String asset = ThemeJsonObject.getPreviewFile(themes[position]);
            if (asset.toLowerCase().equals("error"))
                throw new Exception("Image cannot be loaded, bad position");

            BitmapDrawable b = ((BitmapDrawable) ti.getDrawable());
            if (b != null) b.getBitmap().recycle();
            ti.setImageBitmap(WorldService.decodeSampledBitmapFromFile(ThemeJsonObject.getCustomThemePath(
                    themes[position]) + "/" + asset,
                    (int)getContext().getResources().getDimension(R.dimen.previewWidth),
                    (int)getContext().getResources().getDimension(R.dimen.previewHeight)));
        } catch (Exception e) {
            if(e.getMessage() != null)
                Log.e("error", e.getMessage());
        }
        //ti.setImageResource(resourceImages.getResourceId(position,-1));

        //set checkbox
        AppCompatRadioButton tb = (AppCompatRadioButton) row.findViewById(R.id.ckbox);
        AppCompatImageView iv = (AppCompatImageView) row.findViewById(R.id.lockedIcon);
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