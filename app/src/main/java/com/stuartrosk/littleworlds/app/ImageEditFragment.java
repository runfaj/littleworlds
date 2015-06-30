package com.stuartrosk.littleworlds.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageEditFragment extends Fragment {

    private SharedPreferences preferences;
    private View view;
    private ImageEditFragmentListener listener;
    private ViewGroup radioGroup;
    private Button saveBtn, cancelBtn, sizeBtn, imageBtn;
    private ImageJsonObject config;
    private TextView titleText;
    private ImageView previewImage;

    public ImageEditFragment(){}

    public static ImageEditFragment newInstance(String editPos) {
        ImageEditFragment imageEditFragment = new ImageEditFragment();
        Bundle args = new Bundle();
        args.putString("editPos", editPos);
        imageEditFragment.setArguments(args);
        return imageEditFragment;
    }

    public interface ImageEditFragmentListener {
        public void hideImageEditScreen();
        public boolean isServiceRunning();
        public void cancelImageEditScreen();
        public void onStartImageEdit(String editPos);
        public void onFinishImageEdit();
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
        view = inflater.inflate(R.layout.fragment_image_edit, container, false);

        preferences = getActivity().getSharedPreferences("com.stuartrosk.littleworlds", Context.MODE_PRIVATE);
        String editPos = getArguments().getString("editPos","");

        //add various handlers
        /*
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
        cancelBtn = (Button)view.findViewById(R.id.cancelBtn);
        saveBtn = (Button)view.findViewById(R.id.saveBtn);
        sizeBtn = (Button)view.findViewById(R.id.sizeBtn);
        imageBtn = (Button)view.findViewById(R.id.imageBtn);
        titleText = (TextView)view.findViewById(R.id.imageEditTitleTxt);
        previewImage = (ImageView)view.findViewById(R.id.previewImage);

        config = new ImageJsonObject(getActivity(), ImageJsonObject.Position.valueOf(editPos));

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.cancelImageEditScreen();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //saveRadio();
                config.commitChanges(view.getContext());
                listener.hideImageEditScreen();
            }
        });

        titleText.setText("Editing " + config.getPositionName());
        sizeBtn.setText(config.width + "x" + config.height);

        String imageText = config.file_name;
        if(imageText.equals("")) imageText = getString(R.string.default_image_btn_text);
        imageBtn.setText(imageText);

        //setRadio(config.alignment);

        sizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSizeDialog();
            }
        });

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileDialog();
            }
        });

        setPreviewImage();

        preferences.edit().putString("last_edit_pos",editPos).commit();

        return view;
    }

    @Override
    public void onPause() {
        //if we are supposed to keep service, restart without the edit controls
        listener.onFinishImageEdit();

        super.onPause();
    }

    @Override
    public void onDestroy() {
        //if we are supposed to keep service, restart without the edit controls
        listener.onFinishImageEdit();
        preferences.edit().putString("last_edit_pos","").commit();
        super.onPause();
    }

    @Override
    public void onResume() {
        listener.onStartImageEdit(preferences.getString("last_edit_pos",""));

        super.onResume();
    }

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

    private void showFileDialog() {
        String[] extensions = { ".png", "jpg", ".bmp", ".webp", ".gif"};
        FileDialog fd = new FileDialog(getActivity(), config.file_path, extensions, new FileDialog.FileDialogListener() {
            @Override
            public void fileDialogOutput(String path, String name) {
                config.file_name = name;
                config.file_path = path;
                imageBtn.setText(name);
                setPreviewImage();
            }
        });
        fd.show();
    }

    private void setPreviewImage() {
        previewImage.setImageURI(config.getFullPath(getActivity()));
        previewImage.getLayoutParams().width = config.width * 2;
        previewImage.getLayoutParams().height = config.height * 2;
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