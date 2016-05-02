package com.stuartrosk.borders;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

public class FileListAdapter extends ArrayAdapter<String>{

    private Activity context;
    private List<String> file_name;
    private List<Integer> image_id;
    public FileListAdapter(Activity context,
                      List<String> file_name, List<Integer> image_id) {
        super(context, R.layout.file_row, file_name);
        this.context = context;
        this.file_name = file_name;
        this.image_id = image_id;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.file_row, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.rowtext);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.rowimage);

        txtTitle.setText(file_name.get(position));
        imageView.setImageResource(image_id.get(position));
        return rowView;
    }
}
