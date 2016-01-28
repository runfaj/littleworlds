package com.stuartrosk.borders.app;

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
                theme_image_name,
                file_prefix;

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
            case top_left_corner: return t.file_prefix + "_TLC.png";
            case top_left_middle: return t.file_prefix + "_TLM.png";
            case top_right_middle: return t.file_prefix + "_TRM.png";
            case top_right_corner: return t.file_prefix + "_TRC.png";

            case bottom_left_corner: return t.file_prefix + "_BLC.png";
            case bottom_left_middle: return t.file_prefix + "_BLM.png";
            case bottom_right_middle: return t.file_prefix + "_BRM.png";
            case bottom_right_corner: return t.file_prefix + "_BRC.png";

            case side_left_top: return t.file_prefix + "_SLT.png";
            case side_left_middle: return t.file_prefix + "_SLM.png";
            case side_left_bottom: return t.file_prefix + "_SLB.png";

            case side_right_top: return t.file_prefix + "_SRT.png";
            case side_right_middle: return t.file_prefix + "_SRM.png";
            case side_right_bottom: return t.file_prefix + "_SRB.png";
        }

        return "Error";
    }
}
