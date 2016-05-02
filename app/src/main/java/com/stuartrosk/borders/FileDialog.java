package com.stuartrosk.borders;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileDialog extends Dialog {

    private static String DEFAULT_ROOT = "/";

    private Context context;
    private List<String> path = null;
    private String root = DEFAULT_ROOT;
    private TextView myPath;
    RelativeLayout downloadLink, cameraLink, picturesLink, sdcardLink;
    private ListView list;
    private FileDialogListener fileDialogListener;
    private String[] allowedExtensions = {};

    public interface FileDialogListener {
        void fileDialogOutput(String path, String name, String extension);
    }

    public FileDialog(final Context context, String path, String[] extensions, FileDialogListener fileDialogListener) {
        super(context);
        this.context = context;
        this.fileDialogListener = fileDialogListener;
        if(!path.equals("") && path.startsWith("/"))
            root = path;
        setTitle("Select Image File...");
        if(extensions.length > 0)
            allowedExtensions = extensions;
    }

    private String getPathFromFullPath(String path, String fileName) {
        return path.substring(0, path.indexOf(fileName) - 1);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list_view);
        myPath = (TextView) findViewById(R.id.path);
        list = (ListView) findViewById(R.id.file_list);
        downloadLink = (RelativeLayout) findViewById(R.id.downloadsRow);
        cameraLink = (RelativeLayout) findViewById(R.id.cameraRow);
        picturesLink = (RelativeLayout) findViewById(R.id.picturesRow);
        sdcardLink = (RelativeLayout) findViewById(R.id.sdcardRow);
        getDir(root);

        downloadLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
            }
        });
        cameraLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
            }
        });
        picturesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
            }
        });
        sdcardLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDir(Environment.getExternalStorageDirectory().getAbsolutePath());
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                File file = new File(path.get(position));
                if (file.isDirectory()) {
                    if (file.canRead())
                        getDir(path.get(position));
                    else {
                        new AlertDialog.Builder(context)
                                .setTitle("[" + file.getName() + "] folder can't be read!")
                                .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // TODO Auto-generated method stub
                                            }
                                        }).show();
                    }
                } else {
                    String p = file.getAbsolutePath();
                    p = getPathFromFullPath(p, file.getName());
                    String filenameArray[] = p.split("\\.");
                    String extension = filenameArray[filenameArray.length-1];
                    fileDialogListener.fileDialogOutput(p, file.getName(), extension);
                    dismiss();
                }
            }
        });
    }



    private void getDir(String dirPath)
    {
        String currentDirName = dirPath;
        List<String> item = new ArrayList<>();
        path = new ArrayList<>();
        List<Integer> image = new ArrayList<>();
        File f = new File(dirPath);
        File[] files = f.listFiles();

        Arrays.sort( files, new Comparator()
        {
            public int compare(final Object o1, final Object o2) {
                File a = ((File)o1);
                File b = ((File)o2);
                if(a.isDirectory() && b.isFile()) return -1;
                if(a.isFile() && b.isDirectory()) return 1;
                return a.compareTo(b);
            }
        });

        RelativeLayout
            downloadLinkContainer = downloadLink,
            cameraLinkContainer = cameraLink,
            picturesLinkContainer = picturesLink,
            sdcardLinkContainer = sdcardLink;

        downloadLinkContainer.setVisibility(View.VISIBLE);
        cameraLinkContainer.setVisibility(View.VISIBLE);
        picturesLinkContainer.setVisibility(View.VISIBLE);
        sdcardLinkContainer.setVisibility(View.VISIBLE);

        if(f.getAbsolutePath().equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())) {
            currentDirName = "Downloads";
            downloadLinkContainer.setVisibility(View.GONE);
        } else if(f.getAbsolutePath().equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath())) {
            currentDirName = "Camera";
            cameraLinkContainer.setVisibility(View.GONE);
        } else if(f.getAbsolutePath().equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath())) {
            currentDirName = "Pictures";
            picturesLinkContainer.setVisibility(View.GONE);
        } else if(f.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            currentDirName = "sdcard";
            sdcardLinkContainer.setVisibility(View.GONE);
        }

        myPath.setText("Current Folder: " + currentDirName);

        if (!dirPath.equals(DEFAULT_ROOT))
        {
            item.add("../");
            path.add(f.getParent());
            image.add(R.drawable.ic_reply_black_48dp);
        }

        for (File file : files) {
            if (file.isDirectory()) {
                item.add(file.getName() + "/");
                path.add(file.getPath());
                image.add(R.drawable.ic_folder_black_48dp);
            } else {
                if (allowedExtensions.length > 0) {
                    for (String ext : allowedExtensions) {
                        String str = file.getName().toLowerCase();
                        int lastDot = str.lastIndexOf(".");
                        int matches = str.indexOf(ext.toLowerCase(), lastDot);
                        if (matches > 0) {
                            item.add(file.getName());
                            path.add(file.getPath());
                            image.add(R.drawable.ic_insert_drive_file_black_48dp);
                        }
                    }
                }
            }
        }

        FileListAdapter adapter = new FileListAdapter((Activity)context, item, image);
        list.setAdapter(adapter);
    }

}
