package com.stuartrosk.borders;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.renderscript.ScriptGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageJsonObject {
    public enum SizeType {
        rectangle,
        square
    }
    /*public enum Alignment {
        center,
        topLeft,
        topRight,
        topCenter,
        bottomLeft,
        bottomRight,
        bottomCenter,
        sideLeft,
        sideRight
    }*/
    public enum Position {
        null_position,
        preview,

        top_left_corner,
        top_left_middle,
        top_right_middle,
        top_right_corner,

        bottom_left_corner,
        bottom_left_middle,
        bottom_right_middle,
        bottom_right_corner,

        side_left_top,
        side_left_middle,
        side_left_bottom,

        side_right_top,
        side_right_middle,
        side_right_bottom
    }

    public SizeType type = SizeType.rectangle;
    public Position position = Position.null_position;
    //public Alignment alignment = Alignment.center;
    public int width = 0,
               height = 0;
    public int default_width = 0,
               default_height = 0;
    public String file_name = "",
                  file_path = "";
    SharedPreferences preferences;

    public static String getPositionName(Position position) {
        switch(position) {
            case preview: return "Preview";

            case top_left_corner: return "Top Left Corner";
            case top_left_middle: return "Top Left Center";
            case top_right_middle: return "Top Right Center";
            case top_right_corner: return "Top Right Corner";

            case bottom_left_corner: return "Bottom Left Corner";
            case bottom_left_middle: return "Bottom Left Center";
            case bottom_right_middle: return "Bottom Right Center";
            case bottom_right_corner: return "Bottom Right Corner";

            case side_left_top: return "Left Side Top";
            case side_left_middle: return "Left Side Center";
            case side_left_bottom: return "Left Side Bottom";

            case side_right_top: return "Right Side Top";
            case side_right_middle: return "Right Side Center";
            case side_right_bottom: return "Right Side Bottom";
        }

        return "Error";
    }

    public ImageJsonObject() {}

    public ImageJsonObject setDefaults(Context context, Position p, int width, int height, String file_name, SizeType type) {
        //this is used to initialize a json setting in the event it doesn't already exist

        String default_file_path = context.getString(R.string.default_file_path);
        this.width = default_width = width;
        this.height = default_height = height;
        this.file_name = file_name;
        this.file_path = default_file_path;
        this.type = type;
        this.position = p;

        preferences = context.getSharedPreferences(context.getString(R.string.pref_namespace),Context.MODE_PRIVATE);

        preferences
            .edit()
            .putInt(this.position.toString()+"width", width)
            .putInt(this.position.toString()+"height", height)
        .commit();

        return this;
    }

    public static String getFullPath(Context context, String file_path, String file_name) {
        if(!file_name.equals("")) {
            File file = new File(file_path + "/" + file_name);
            if (file.exists())
                return file.toString();
        }
        return null;
    }

    public static InputStream getFileStream(Context context, Uri uri) {
        if(uri != null) {
            try {
                return context.getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {}
        }
        return null;
    }
}
