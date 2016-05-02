package com.stuartrosk.borders;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ThemeJsonObject {
    private static String themesFile = "themes.json";

    public Theme[] themes = {};

    public class Theme {
        public int id;
        public String title,
                file_prefix;

        public boolean paid_content = true;
    }

    private static String loadJSONFromAsset(Context context, String file_name) {
        String json;
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

    private static String loadJSONFromFile(Context context, String file_name){
        String json;
        try {
            FileInputStream is = new FileInputStream(file_name);
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
            for(Theme t: i.themes) {
                if(t.id == id)
                    return t;
            }
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
        return null;
    }

    public static Theme[] getCustomThemes(Context context){
        ArrayList<Theme> themes = new ArrayList<>();

        File bordersDir = Environment.getExternalStorageDirectory();
        File temp = new File(bordersDir.getAbsolutePath() + "/Borders");
        if(!temp.exists() || !temp.isDirectory())
            temp.mkdir();
        bordersDir = temp;

        File temp2 = new File(bordersDir.getAbsolutePath() + "/Themes");
        if(!temp2.exists() || !temp2.isDirectory())
            temp2.mkdir();
        bordersDir = temp2;

        File f = new File(bordersDir.toString());
        File[] files = f.listFiles();
        Arrays.sort(files);
        for (File inFile : files) {
            if (inFile.isDirectory()) {
                Log.d("tjo reading",inFile.toString());
                Theme t = getThemeFromFile(context, inFile.getAbsolutePath() + "/manifest.json");
                if(t != null && t.title != null && t.file_prefix != null) {
                    Log.d("customt",t.title);
                    t.id = Integer.parseInt(inFile.getName());
                    themes.add(t);
                }
            }
        }

        return themes.toArray(new Theme[themes.size()]);
    }

    public static Theme getCustomTheme(Context context, int id) {
        Theme t = getThemeFromFile(context, Environment.getExternalStorageDirectory().getAbsolutePath() + "/Borders/Themes/" + id + "/manifest.json");
        if(t != null)
            t.id = id;
        return t;
    }

    public static boolean writeCustomManifest(Context context, Theme manifest) {
       Log.d("manifest","writing custom to /Themes");
        boolean success = true;
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Borders/Themes/" + manifest.id + "/manifest.json";
            Log.d(manifest.title, path);
            FileOutputStream overWrite = new FileOutputStream(path, false);
            String json =
                    "{\n" +
                    "    title: \""+manifest.title+"\",\n" +
                    "    file_prefix: \""+manifest.file_prefix+"\"\n" +
                    "}";
            overWrite.write(json.getBytes());
            overWrite.flush();
            overWrite.close();
        } catch (Exception e) {
            success = false;
        }
        return success;
    }

    public static File copyAsset(Activity activity, String outpath, String filename) throws IOException {
        Log.d("asset","copy asset "+filename);
        AssetManager assetManager = activity.getAssets();
        String[] files = assetManager.list("");
        for(String fname : files) {
            if (fname.equals(filename)) {
                Log.d("asset","list asset "+fname);
                InputStream in = assetManager.open(fname);
                File outFile = new File(outpath, fname);
                OutputStream out = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int read;
                while((read = in.read(buffer)) != -1){
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();

                return outFile;
            }
        }
        return null;
    }

    public static String zipCustomTheme(Activity activity, Theme theme) {
        int BUFFER = 2048;
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
        String ts = currentTimestamp.toString().replaceAll(":","-").replaceAll(" ","_");
        String _zipFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Borders/" + theme.title + "__" + ts + ".btheme";
        String themeDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Borders/Themes/" + theme.id;
        String output = "";

        try  {
            File f = new File(themeDir);
            File[] files = f.listFiles();

            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(_zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            //add all files in directory
            for (File file : files) {
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER);
                String fname = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/") + 1);
                if (fname.equals("README.txt") || fname.equals("border_template.ai"))
                    continue;
                ZipEntry entry = new ZipEntry(fname);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            //add readme file
            File readme = copyAsset(activity, Environment.getExternalStorageDirectory().getAbsolutePath() + "/Borders", "README.txt");
            if(readme != null) {
                FileInputStream rfi = new FileInputStream(readme);
                origin = new BufferedInputStream(rfi, BUFFER);
                ZipEntry rentry = new ZipEntry(readme.getAbsolutePath().substring(readme.getAbsolutePath().lastIndexOf("/") + 1));
                out.putNextEntry(rentry);
                int rcount;
                while ((rcount = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, rcount);
                }
                origin.close();
            }

            //add template file
            File template = copyAsset(activity, Environment.getExternalStorageDirectory().getAbsolutePath() + "/Borders", "border_template.ai");
            if(template != null) {
                FileInputStream tfi = new FileInputStream(template);
                origin = new BufferedInputStream(tfi, BUFFER);
                ZipEntry tentry = new ZipEntry(template.getAbsolutePath().substring(template.getAbsolutePath().lastIndexOf("/") + 1));
                out.putNextEntry(tentry);
                int tcount;
                while ((tcount = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, tcount);
                }
                origin.close();
            }


            out.close();
            output = _zipFile;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    //recursive
    private static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return( path.delete() );
    }

    public static boolean deleteCustomTheme(Context context, Theme theme) {
        File path = new File(getCustomThemePath(theme));
        return deleteDirectory(path);
    }

    public static Theme getThemeFromFile(Context context, String path) {
        Gson gson = new GsonBuilder().create();
        Theme i;
        try {
            i = gson.fromJson(loadJSONFromFile(context,path), Theme.class);
            return i;
        } catch (Exception e) {
            Log.e("error", e.getMessage());
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

    public static String getPreviewFile(Theme t){
        return t.file_prefix + "_preview.png";
    }

    public static String getFileFromPosition(Theme t, ImageJsonObject.Position p) {
        switch(p) {
            case preview: return getPreviewFile(t);

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

    public static String getCustomThemePath(Theme t){
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Borders/Themes/" + t.id;
    }
}
