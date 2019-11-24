package com.example.ghazanfer.messenger_fin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CustomBottomSheetDialogFragment extends BottomSheetDialogFragment {

    LinearLayout cameraButton;
    LinearLayout documentButton;
    LinearLayout galleryButton;
    final int CAMERA_PIC_TAKEN=165;
    Communicator comm;
    public String receiver;
    public String[] allowedFilesArray={".pdf",".apk",".mp3",".docx",".pptx",".xlsx"};//.mp4 another option in main  dialog frag
    public int[] resource_doc={R.drawable.pdf_icon,R.drawable.apk_icon,R.drawable.mp3_icon,R.drawable.docx_icon,
            R.drawable.pptx_icon,R.drawable.xlsx_icon};
    String[] fileNameArray;
    List<File> myFilesList;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        comm= (Communicator) getActivity();
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_custom_bottom_sheet_dialog, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }


        //Adding button functionality here
        cameraButton=contentView.findViewById(R.id.cameraButton);
        galleryButton=contentView.findViewById(R.id.galleryButton);
        documentButton=contentView.findViewById(R.id.documentButton);


        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                intent.putExtra("receiver",receiver);
                frag_messaging_Activity.receiverForImageSending=receiver;
                getActivity().startActivityForResult(intent,CAMERA_PIC_TAKEN);//CAMERA_PIC_TAKEN representing both gallery pickup and image capture
                dismiss();
//                CropImage.activity()
//                        .start(getContext(), CustomBottomSheetDialogFragment.this);
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");

                frag_messaging_Activity.receiverForImageSending=receiver;
//                intent.putExtra("receiver",receiver);
                getActivity().startActivityForResult(intent,CAMERA_PIC_TAKEN);//CAMERA_PIC_TAKEN representing both gallery pickup and image capture
                dismiss();
            }
        });

        documentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog=new Dialog(getActivity(),android.R.style.Theme_Black);
                dialog.setContentView(R.layout.mylist);
                ListView lv=dialog.findViewById(R.id.mylistfiles);
                final File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath());
//                Log.d("mytagerz", file.getPath()+"   "+file.getName());
                myFilesList=getListFiles(file);

                myDocumentsAdapter docAdapter=new myDocumentsAdapter();
                lv.setAdapter(docAdapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String fileNameStr=myFilesList.get(i).getName();
                        String mime_type=null;
                        for(int counter=0;counter<allowedFilesArray.length;counter++) {
                            if (fileNameStr.endsWith(allowedFilesArray[counter])){
                                mime_type=allowedFilesArray[counter];
                                break;
                            }
                        }
                        Log.d("yolocheck", "onItemClick: "+receiver);
                        comm.sendFile(myFilesList.get(i),mime_type,receiver);
                        comm.fileSentNotify();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }
    private List<File> getListFiles(File parentDir) {
        List<File> inFiles = new ArrayList<>();
        Queue<File> files = new LinkedList<>();
        files.addAll(Arrays.asList(parentDir.listFiles()));
        while (!files.isEmpty()) {
            File file = files.remove();
//            Log.d("myyolo", file.getPath()+"   "+file.getName());
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            } else {
                for(int i=0;i<allowedFilesArray.length;i++) {
                    if (file.getName().endsWith(allowedFilesArray[i])) {
                        inFiles.add(file);
                    }
                };
            }
        }
        return inFiles;
    }
    class myDocumentsAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return myFilesList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view= getActivity().getLayoutInflater().inflate(R.layout.custom_layout_singleitem_documents,null);
            ImageView media_icon=view.findViewById(R.id.media_icon);
            TextView fileName=view.findViewById(R.id.fileName);
            TextView fileSize=view.findViewById(R.id.fileSize);
            TextView fileDateModified=view.findViewById(R.id.fileDateModified);
            String fileNameStr=myFilesList.get(i).getName();

            for(int counter=0;counter<allowedFilesArray.length;counter++) {
                if (fileNameStr.endsWith(allowedFilesArray[counter])){
                    media_icon.setImageResource(resource_doc[counter]);
                    break;
                }
            }
            if(fileNameStr.length()>18){
                fileNameStr=fileNameStr.substring(0,18);
                fileNameStr+="...";
            }
            fileName.setText(fileNameStr);
            Float myfileSize=Float.parseFloat(String.valueOf(myFilesList.get(i).length()/1048576.0));
            fileSize.setText(String.format ("%.2f", myfileSize)+" mb");
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
            fileDateModified.setText(sdf.format(myFilesList.get(i).lastModified()));
            return view;
        }
    }
}