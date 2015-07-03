package com.stuartrosk.littleworlds.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
    private String[] resourceNames = null;
    private String[] resourceImages = null;
    private String[] resourceValues = null;
    private ThemeListPreference ts;

    public ThemeListAdapter(Context context, int textViewResourceId,
                            CharSequence[] objects, String[] ids,
                            String[] texts, String[] images, int i, ThemeListPreference ts) {
        super(context, textViewResourceId, objects);

        selected = i;
        resourceNames = texts;
        resourceImages = images;
        resourceValues = ids;
        this.ts = ts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //inflate layout
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        View row = inflater.inflate(R.layout.image_list_row, parent, false);

        //set id
        row.setId(Integer.parseInt(resourceValues[position]));

        //set on click listener for row
        row.setOnClickListener(this);

        //set name
        TextView tv = (TextView) row.findViewById(R.id.themeName);
        tv.setText(resourceNames[position]);

        //set image
        ImageView ti = (ImageView) row.findViewById(R.id.themeImage);
        try {
            ti.setImageDrawable(
                    Drawable.createFromStream(
                            getContext().getAssets().open(resourceImages[position]),
                            null
                    )
            );
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
        //ti.setImageResource(resourceImages.getResourceId(position,-1));

        //set checkbox
        RadioButton tb = (RadioButton) row.findViewById(R.id.ckbox);
        if (Integer.parseInt(resourceValues[position]) == selected) {
            tb.setChecked(true);
        } else {
            tb.setChecked(false);
        }
        tb.setClickable(false);

        return row;
    }

    @Override
    public void onClick(View v)
    {
        ts.setResult(v.getId());
    }
}