package com.stuartrosk.littleworlds.app;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;

public class ThemeJsonObject {
    private static String themesFile = "themes.json";

    public Theme[] themes = {};

    public class Theme {
        public int id;
        public String title,
                theme_image_name;

        public String
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
                side_right_bottom;

        public boolean paid_content = true;
    }

    private static String loadJSONFromAsset(Context context, String file_name) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(file_name);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return "{}";
        }
        return json;
    }

    public static Theme getTheme(Context context, int id) {
        Gson gson = new GsonBuilder().create();
        ThemeJsonObject i = null;
        try {
            i = gson.fromJson(loadJSONFromAsset(context,themesFile), ThemeJsonObject.class);
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
        for(Theme t: i.themes) {
            if(t.id == id)
                return t;
        }
        return null;
    }

    public static Theme[] getThemes(Context context) {
        Gson gson = new GsonBuilder().create();
        ThemeJsonObject i = new ThemeJsonObject();
        try {
            i = gson.fromJson(loadJSONFromAsset(context,themesFile), ThemeJsonObject.class);
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }

        return i.themes;
    }

    public static String getFileFromPosition(Theme t, ImageJsonObject.Position p) {
        switch(p) {
            case top_left_corner: return t.top_left_corner;
            case top_left_middle: return t.top_left_middle;
            case top_right_middle: return t.top_left_middle;
            case top_right_corner: return t.top_right_corner;

            case bottom_left_corner: return t.bottom_left_corner;
            case bottom_left_middle: return t.bottom_left_middle;
            case bottom_right_middle: return t.bottom_right_middle;
            case bottom_right_corner: return t.bottom_right_corner;

            case side_left_top: return t.side_left_top;
            case side_left_middle: return t.side_left_middle;
            case side_left_bottom: return t.side_left_bottom;

            case side_right_top: return t.side_right_top;
            case side_right_middle: return t.side_right_middle;
            case side_right_bottom: return t.side_right_bottom;
        }

        return "Error";
    }
}
