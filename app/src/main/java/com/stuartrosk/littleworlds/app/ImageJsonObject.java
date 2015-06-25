package com.stuartrosk.littleworlds.app;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ImageJsonObject {
    private static String EMPTY_JSON_VALUE = "{}";

    public enum SizeType {
        rectangle,
        square
    }
    public enum Alignment {
        center,
        topLeft,
        topRight,
        topCenter,
        bottomLeft,
        bottomRight,
        bottomCenter,
        sideLeft,
        sideRight
    }
    public enum Position {
        null_position,

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
    public Alignment alignment = Alignment.center;
    public int width = 0,
               height = 0;
    public String file_name = "",
                  file_path = "";

    public String getPositionName() {
        switch(position) {
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

    //use this constructor when manually setting defaults
    public ImageJsonObject() {}

    //use this constructor when just getting existing config, not setting defaults
    public ImageJsonObject(Context context, Position p) {
        readPreferences(context, p);
    }

    public static ImageJsonObject JsonToObject(String json) {
        Gson gson = new GsonBuilder().create();
        ImageJsonObject i = null;
        try {
            i = gson.fromJson(json, ImageJsonObject.class);
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
        return i;
    }

    public static String ObjectToJson(ImageJsonObject i) {
        Gson gson = new GsonBuilder().create();
        String json = "{}";
        try {
            json = gson.toJson(i, ImageJsonObject.class);
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
        return json;
    }

    public ImageJsonObject setDefaults(Context context, Position p, int width, int height, String file_name, SizeType type) {
        //this is used to initialize a json setting in the event it doesn't already exist

        String default_file_path = context.getString(R.string.default_file_path);
        String json = readPreferences(context, p);
        if(json.equals(EMPTY_JSON_VALUE)) {
            this.width = width;
            this.height = height;
            this.file_name = file_name;
            this.file_path = default_file_path;
            this.type = type;
            this.position = p;
            commitChanges(context);
        }

        return this;
    }

    public String readPreferences(Context context, Position p) {
        String curJson = context.getSharedPreferences("com.stuartrosk.littleworlds",Context.MODE_PRIVATE).getString(p.toString(),EMPTY_JSON_VALUE);
        //Log.d("test",curJson);
        ImageJsonObject newObj = JsonToObject(curJson);


        width = newObj.width;
        height = newObj.height;
        file_name = newObj.file_name;
        file_path = newObj.file_path;
        type = newObj.type;
        position = newObj.position;
        alignment = newObj.alignment;

        return curJson;
    }

    public void commitChanges(Context context) {
        String json = ObjectToJson(this);
        context.getSharedPreferences("com.stuartrosk.littleworlds",Context.MODE_PRIVATE)
            .edit()
            .putString(this.position.toString(), json)
            .commit();
    }
}
