package com.stuartrosk.littleworlds.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
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
    private TypedArray resourceImages = null;
    private int[] resourceValues = null;
    private EditListPreference ts;

    public ThemeListAdapter(Context context, int textViewResourceId,
                            CharSequence[] objects, int[] ids,
                            String[] texts, TypedArray images, int i, EditListPreference ts) {
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
        row.setId(resourceValues[position]);

        //set on click listener for row
        row.setOnClickListener(this);

        //set name
        TextView tv = (TextView) row.findViewById(R.id.themeName);
        tv.setText(resourceNames[position]);

        //set image
        ImageView ti = (ImageView) row.findViewById(R.id.themeImage);
        ti.setImageResource(resourceImages.getResourceId(position,-1));

        //set checkbox
        RadioButton tb = (RadioButton) row.findViewById(R.id.ckbox);
        if (resourceValues[position] == selected) {
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