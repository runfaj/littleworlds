package com.stuartrosk.littleworlds.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileDialog extends Dialog {

    private static String DEFAULT_ROOT = "/";

    private Context context;
    private List<String> item = null;
    private List<String> path = null;
    private String root = DEFAULT_ROOT;
    private TextView myPath;
    private ListView list;
    private FileDialogListener fileDialogListener;
    private String[] allowedExtensions = {};

    public interface FileDialogListener {
        public void fileDialogOutput(String path, String name);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list_view);
        myPath = (TextView) findViewById(R.id.path);
        list = (ListView) findViewById(R.id.file_list);
        getDir(root);

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
                    p = p.substring(0,p.indexOf(file.getName())-1);
                    Log.d("file",p + " " + file.getName() );
                    fileDialogListener.fileDialogOutput(p, file.getName());
                    dismiss();
                }
            }
        });
    }



    private void getDir(String dirPath)
    {
        myPath.setText("Location: " + dirPath);
        item = new ArrayList<String>();
        path = new ArrayList<String>();
        File f = new File(dirPath);
        File[] files = f.listFiles();

        if (!dirPath.equals(DEFAULT_ROOT))
        {
            item.add("../");
            path.add(f.getParent());
        }

        for (int i = 0; i < files.length; i++)
        {
            File file = files[i];
            if (file.isDirectory()) {
                item.add(file.getName() + "/");
                path.add(file.getPath());
            } else {
                if(allowedExtensions.length > 0) {
                    for(String ext: allowedExtensions) {
                        String str = file.getName().toLowerCase();
                        int lastDot = str.lastIndexOf(".");
                        int matches = str.indexOf(ext.toLowerCase(),lastDot);
                        if(matches > 0) {
                            item.add(file.getName());
                            path.add(file.getPath());
                        }
                    }
                }
            }
        }

        ArrayAdapter<String> fileList =
                new ArrayAdapter<String>(context, R.layout.file_row, item);
        list.setAdapter(fileList);
    }

}
