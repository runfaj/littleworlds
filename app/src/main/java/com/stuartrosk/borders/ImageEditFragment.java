package com.stuartrosk.borders;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


import java.io.*;

public class ImageEditFragment extends SuperFragment {

    private SharedPreferences preferences;
    private ImageJsonObject.Position position;
    private ThemeJsonObject.Theme currTheme;
    private ImageEditFragmentListener listener;
    private AppCompatButton imageBtn;
    private InputStream selectedFileIS;
    private Uri selectedFileUri;
    private AppCompatImageView previewImage, imageClear;

    private int PICK_IMAGE_REQUEST = 1;

    private boolean cancelCheck = false;

    public ImageEditFragment(){}

    public static ImageEditFragment newInstance(String editPos) {
        ImageEditFragment imageEditFragment = new ImageEditFragment();
        Bundle args = new Bundle();
        args.putString("editPos", editPos);
        imageEditFragment.setArguments(args);
        return imageEditFragment;
    }

    public interface ImageEditFragmentListener {
        void hideImageEditScreen();
        boolean isServiceRunning();
        void onStartImageEdit(String editPos);
        void onFinishImageEditDestroy();
        boolean appPermissions(boolean requestPerms);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (ImageEditFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ImageEditFragmentListener");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_edit, container, false);

        preferences = getActivity().getSharedPreferences(getString(R.string.pref_namespace), Context.MODE_PRIVATE);
        String editPos = getArguments().getString("editPos", "");
        position = ImageJsonObject.Position.valueOf(editPos);
        currTheme = ThemeJsonObject.getCustomTheme(getActivity().getApplicationContext(), preferences.getInt(getActivity().getApplicationContext().getString(R.string.custom_id),0));

        //add various handlers
        /*****************
        radioGroup = (ViewGroup) view.findViewById(R.id.edit_radiogroup);
        for(int i=0;i<radioGroup.getChildCount();i++) {
            if(radioGroup.getChildAt(i) instanceof RadioButton) {
                radioGroup.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleRadios((RadioButton)view.findViewById(v.getId()));
                    }
                });
            }
        }
        */
        AppCompatButton cancelBtn = (AppCompatButton) view.findViewById(R.id.cancelBtn);
        AppCompatButton saveBtn = (AppCompatButton) view.findViewById(R.id.saveBtn);
        AppCompatButton resetBtn = (AppCompatButton) view.findViewById(R.id.resetBtn);
        //sizeBtn = (Button)view.findViewById(R.id.sizeBtn);
        imageBtn = (AppCompatButton) view.findViewById(R.id.imageBtn);
        AppCompatTextView titleText = (AppCompatTextView) view.findViewById(R.id.imageEditTitleTxt);
        previewImage = (AppCompatImageView) view.findViewById(R.id.previewImage);
        imageClear = (AppCompatImageView) view.findViewById(R.id.imageClear);

        //config = new ImageJsonObject(getActivity(), ImageJsonObject.Position.valueOf(editPos));

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            cancelCheck = true;
            listener.hideImageEditScreen();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //saveRadio();
                if(commitChanges()) {
                    cancelCheck = true;
                    listener.hideImageEditScreen();
                }
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetDialog();
            }
        });

        titleText.setText("Editing " + ImageJsonObject.getPositionName(position));
        //sizeBtn.setText(config.width + "x" + config.height);

        /***********String imageText = config.file_name;
        if(imageText.equals("")) {
            imageText = getString(R.string.default_image_btn_text);
        } else {
            imageClear.setVisibility(View.VISIBLE);
        }
        imageBtn.setText(imageText);*/

        //setRadio(config.alignment);

        /*sizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSizeDialog();
            }
        });*/
        imageClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetImageSource();
            }
        });
        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileDialog();
            }
        });

        Uri path = Uri.parse(ThemeJsonObject.getCustomThemePath(currTheme) + "/" + ThemeJsonObject.getFileFromPosition(currTheme, position));
        setPreviewImage(ImageJsonObject.getFileStream(getActivity().getApplicationContext(), path));

        preferences.edit().putString("last_edit_pos", editPos).commit();

        sendView(getClass().getSimpleName());

        return view;
    }

    @Override
    public void onPause() {
        //if we are supposed to keep service, restart without the edit controls
        if(!cancelCheck)
            listener.onFinishImageEditDestroy();

        super.onPause();
    }

    @Override
    public void onDestroy() {
        //if we are supposed to keep service, restart without the edit controls
        if(!cancelCheck) {
            preferences.edit().putString("last_edit_pos", "").commit();
            listener.onFinishImageEditDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        if(listener.appPermissions(false)) {
            listener.onStartImageEdit(preferences.getString("last_edit_pos", ""));
        } else {
            listener.hideImageEditScreen();
        }

        super.onResume();
    }

    private void showResetDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.reset_title));
        alertDialogBuilder
                .setMessage(getString(R.string.reset_message))
                .setNegativeButton(getString(R.string.reset_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.reset_go), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //reset it
                        String path = ThemeJsonObject.getCustomThemePath(currTheme) + "/" + ThemeJsonObject.getFileFromPosition(currTheme, position);
                        File existingFile = new File(path);
                        if(existingFile.isFile() && !existingFile.isDirectory()) {
                            existingFile.delete();
                        }

                        dialog.dismiss();
                        cancelCheck = true;
                        listener.hideImageEditScreen();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public static Bitmap decodeSampledBitmapFromInputStream(InputStream in,
                                                            InputStream copyOfin, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(copyOfin, null, options);
    }

    private boolean commitChanges(){
        boolean success = false;

        //if no new file, just return success
        if(selectedFileUri == null)
            return true;

        String newFile = ThemeJsonObject.getCustomThemePath(currTheme) + "/" + ThemeJsonObject.getFileFromPosition(currTheme, position);
        try {
            if(selectedFileUri != null){
                OutputStream out = new FileOutputStream(newFile);

                /*Bitmap in = decodeSampledBitmapFromFile(selectedFilePath,
                        preferences.getInt(position.toString()+"width",10),
                        preferences.getInt(position.toString()+"height",10));*/
                Bitmap in = decodeSampledBitmapFromInputStream(
                        ImageJsonObject.getFileStream(getActivity().getApplicationContext(), selectedFileUri),
                        ImageJsonObject.getFileStream(getActivity().getApplicationContext(), selectedFileUri),
                        preferences.getInt(position.toString()+"width",10),
                        preferences.getInt(position.toString()+"height",10));
                in.compress(Bitmap.CompressFormat.PNG, 100, out);

                out.close();

                Log.v("blah", "Copy file successful.");
                success = true;

            }else{
                Log.v("blah", "Copy file failed. Source file missing.");
                Toast.makeText(getActivity().getApplicationContext(),"Unable to find selected file.",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return success;
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /************************
    private void showSizeDialog() {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View textEntryView = factory.inflate(R.layout.size_dialog, null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setView(textEntryView);

        final EditText numHeight = (EditText) textEntryView.findViewById(R.id.numHeight);
        final EditText numWidth = (EditText) textEntryView.findViewById(R.id.numWidth);
        final Button resetBtn = ((Button) textEntryView.findViewById(R.id.btnResetSize));

        numHeight.setText(String.valueOf(config.height), TextView.BufferType.EDITABLE);
        numWidth.setText(String.valueOf(config.width), TextView.BufferType.EDITABLE);

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numWidth.setText(String.valueOf(config.default_width));
                numHeight.setText(String.valueOf(config.default_height));
            }
        });

        alert.setTitle("Enter Sizes:")
                .setPositiveButton("Apply",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String h = numHeight.getText().toString();
                                String w = numWidth.getText().toString();
                                int width = Integer.parseInt(w);
                                int height = Integer.parseInt(h);

                                if (width > 1) config.width = width;
                                if (height > 1) config.height = height;
                                sizeBtn.setText(config.width + "x" + config.height);

                                setPreviewImage();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        });

        final AlertDialog a = alert.create();
        numWidth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        a.show();
    }
    ******/

    private void resetImageSource() {
        imageBtn.setText(getString(R.string.default_image_btn_text));
        Uri path = Uri.parse(ThemeJsonObject.getCustomThemePath(currTheme) + "/" + ThemeJsonObject.getFileFromPosition(currTheme, position));
        setPreviewImage(ImageJsonObject.getFileStream(getActivity().getApplicationContext(),path));
        imageClear.setVisibility(View.GONE);
    }

    private void showFileDialog() {
        String[] extensions = { ".png", "jpg", ".bmp", ".webp", ".gif"};
        /*FileDialog fd = new FileDialog(getActivity(), Environment.getExternalStorageDirectory().getAbsolutePath(), extensions, new FileDialog.FileDialogListener() {
            @Override
            public void fileDialogOutput(String path, String name, String ext) {
                selectedFilePath = ImageJsonObject.getFullPath(getActivity().getApplicationContext(), path, name);
                if(selectedFilePath == null)
                    Toast.makeText(getActivity().getApplicationContext(), "The file chosen for " + ImageJsonObject.getPositionName(position) + " is not valid.", Toast.LENGTH_LONG).show();
                else {
                    imageBtn.setText(name);
                    setPreviewImage(selectedFilePath);
                    imageClear.setVisibility(View.VISIBLE);
                }
            }
        });
        fd.show();*/

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {

            selectedFileUri = data.getData();

            try {
                Log.d("ief ijo",selectedFileUri.toString());
                selectedFileIS = ImageJsonObject.getFileStream(getActivity().getApplicationContext(), selectedFileUri);
                if(selectedFileIS == null)
                    Toast.makeText(getActivity().getApplicationContext(), "The file chosen for " + ImageJsonObject.getPositionName(position) + " is not valid.", Toast.LENGTH_LONG).show();
                else {
                    imageBtn.setText(MediaStore.Images.Media.TITLE);
                    setPreviewImage(selectedFileIS);
                    imageClear.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setPreviewImage(InputStream selectedFilePath) {
        previewImage.setImageBitmap(BitmapFactory.decodeStream(selectedFilePath));
        prevImageCommon();
    }
    /*private void setPreviewImage(String selectedFilePath) {
        previewImage.setImageURI(Uri.parse(selectedFilePath));
        prevImageCommon();
    }*/
    private void prevImageCommon(){
        previewImage.getLayoutParams().width = preferences.getInt(position.toString()+"width",10) * 2; //config.width * 2;
        previewImage.getLayoutParams().height = preferences.getInt(position.toString()+"height",10) * 2; //config.height * 2;
        if(position == ImageJsonObject.Position.preview) {
            previewImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            previewImage.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        previewImage.requestLayout();
    }

    /*
    private void handleRadios(RadioButton selected) {
        for(int i=0;i<radioGroup.getChildCount();i++) {
            if(radioGroup.getChildAt(i) instanceof RadioButton) {
                if(!radioGroup.getChildAt(i).equals(selected))
                    ((RadioButton) radioGroup.getChildAt(i)).setChecked(false);
            }
        }
    }

    private void setRadio(ImageJsonObject.Alignment a) {
        RadioButton checked = null;
        switch (a) {
            case center: checked = (RadioButton)view.findViewById(R.id.radioC); break;
            case topLeft: checked = (RadioButton)view.findViewById(R.id.radioTL); break;
            case topCenter: checked = (RadioButton)view.findViewById(R.id.radioTM); break;
            case topRight: checked = (RadioButton)view.findViewById(R.id.radioTR); break;
            case bottomLeft: checked = (RadioButton)view.findViewById(R.id.radioBL); break;
            case bottomCenter: checked = (RadioButton)view.findViewById(R.id.radioBM); break;
            case bottomRight: checked = (RadioButton)view.findViewById(R.id.radioBR); break;
            case sideLeft: checked = (RadioButton)view.findViewById(R.id.radioLM); break;
            case sideRight: checked = (RadioButton)view.findViewById(R.id.radioRM); break;
        }
        checked.setChecked(true);
        handleRadios(checked);
    }

    private void saveRadio() {
        RadioButton checked = null;
        for(int i=0;i<radioGroup.getChildCount();i++) {
            if(radioGroup.getChildAt(i) instanceof RadioButton) {
                if(((RadioButton) radioGroup.getChildAt(i)).isChecked())
                    checked = (RadioButton)radioGroup.getChildAt(i);
            }
        }
        Log.d("test",checked.getTag().toString());
        config.alignment = ImageJsonObject.Alignment.valueOf(checked.getTag().toString());
    }
    */
}