package com.example.ghazanfer.messenger_fin;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class settings extends AppCompatActivity {
    ListView mySettingListView;
    String username;
    String fullname;
    TextView username_nonedit;
    TextView fullname_edit;
    RelativeLayout myButtonRlayout;
    SharedPreferences mySharedPreferences;
    private int PICK_IMAGE_REQUEST=1;
    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE=100;
    Uri filepathuri;
    String login_unique_id;
    ImageView myProfileImage;
    SharedPreferences.Editor editor;
    Button settingBackButton;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        mySettingListView= findViewById(R.id.mySettingListView);
        final String[] arr={"hello","world","this","test","woah","lalal","opbyh"};
        ArrayAdapter<String> x=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arr);
        username_nonedit= findViewById(R.id.username_nonedit);
        fullname_edit=findViewById(R.id.fullname_edit);
        myButtonRlayout=findViewById(R.id.myButtonRlayout);
        myProfileImage=findViewById(R.id.picsetting);
        settingBackButton=findViewById(R.id.settingBackButton);
        settingBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                //do something
            }
        });

        myButtonRlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChoser();
            }
        });

        int mode= Activity.MODE_PRIVATE;
        mySharedPreferences=getSharedPreferences("MySharedPreference",mode);
        editor=mySharedPreferences.edit();
        username=mySharedPreferences.getString("username", null);
        fullname=mySharedPreferences.getString("fullname", null);
        username_nonedit.setText("@"+username);
        fullname_edit.setText(fullname);
        mySettingListView.setAdapter(x);

        getProfilePathFromServer();
        String profileUrl=mySharedPreferences.getString("profileImageUrl",null);
        if(profileUrl!=null)
        Glide.with(this).
                load(okhttpSingleton.MYBASEURL+profileUrl).
                apply(new RequestOptions().placeholder(R.drawable.placeholder).error(R.drawable.placeholder)).
                into(myProfileImage);
    }

    private void showFileChoser() {
//        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setType("image/*");
//        intent.putExtra("aspectX",1);
//        intent.putExtra("aspectY",1);
//        intent.putExtra("scale",true);
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//        startActivityForResult(intent,PICK_IMAGE_REQUEST);
//        Log.d("mine tag", "showFileChoser: ");

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1)
                .start(this);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filepathuri = result.getUri();
                if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP_MR1){
                    //MEANS PERMISSION IS GRANTED BY THE MANIFEST
                    prepareImage();
                }
                else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        Log.d("mtripermission", "onActivityResult: PERMISSION  NOT GRANTED");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {

                        } else {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                        }
                    }
                    else {
                    // Permission has already been granted
                    Log.d("mtripermission", "onActivityResult: PERMISSION GRANTED");
                            prepareImage();

                }
            }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("exceptioncropimage", "onActivityResult: "+error.getMessage());
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        prepareImage();
                    // permission was granted, yay! Do the
                } else {
                    // permission denied, boo!
                    Toast.makeText(this, "Sorry cannot upload as the permission for read is not granted", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

    public void prepareImage(){
        try {
            File file = new File(getPath(filepathuri));
            File compressedImageFile = new Compressor(this).compressToFile(file);
            uploadProfileImage(compressedImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void uploadProfileImage(File file){

        login_unique_id=mySharedPreferences.getString("login_unique_id","null");

        final OkHttpClient client=okhttpSingleton.gethttpSingleton().getOkHttpClient();
        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        RequestBody requestBody=new MultipartBody.Builder().
                setType(MultipartBody.FORM).
                addFormDataPart("special_key", okhttpSingleton.SPECIAL_ACCESS_KEY).
                addFormDataPart("operation", "profilePicUpload").
                addFormDataPart("username", username).
                addFormDataPart("login_unique_id", login_unique_id).
                addFormDataPart("userProfileImage","profile.png",RequestBody.create(MEDIA_TYPE_PNG,file)).
                build();
        final Request request = new Request.Builder()
                .url(okhttpSingleton.MYURL)
                .post(requestBody)
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response=client.newCall(request).execute();
                    final String responseStr=response.body().string();
                    Log.d("Profileimagecallresp", responseStr);

                    final JSONObject responseObj=new JSONObject(responseStr);
                    if(responseObj.getString("status").equals("Success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Glide.with(settings.this).
                                        load(okhttpSingleton.MYBASEURL + responseObj.getString("message")).into(myProfileImage);
                                    editor.putString("profileImageUrl",responseObj.getString("message"));
                                    editor.commit();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(settings.this,responseObj.getString("message") , Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }
    public void getProfilePathFromServer(){
        login_unique_id=mySharedPreferences.getString("login_unique_id","null");

        final OkHttpClient client=okhttpSingleton.gethttpSingleton().getOkHttpClient();
        RequestBody requestBody=new MultipartBody.Builder().
                setType(MultipartBody.FORM).
                addFormDataPart("special_key", okhttpSingleton.SPECIAL_ACCESS_KEY).
                addFormDataPart("operation", "getProfilePic").
                addFormDataPart("username", username).
                addFormDataPart("login_unique_id", login_unique_id).
                build();
        final Request request = new Request.Builder()
                .url(okhttpSingleton.MYURL)
                .post(requestBody)
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response=client.newCall(request).execute();
                    final String responseStr=response.body().string();
                    Log.d("Profileimagecallresp", responseStr);

                    final JSONObject responseObj=new JSONObject(responseStr);
                    if(responseObj.getString("status").equals("Success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Glide.with(settings.this).
                                            load(okhttpSingleton.MYBASEURL + responseObj.getString("message")).into(myProfileImage);
                                    editor.putString("profileImageUrl",responseObj.getString("message"));
                                    editor.commit();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(settings.this,responseObj.getString("message") , Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String getPath(Uri uri) {
            return uri.getPath();
    }




}
