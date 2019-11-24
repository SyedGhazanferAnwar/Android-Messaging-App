//package com.example.ghazanfer.messenger_fin;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.provider.MediaStore;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Base64;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.ByteArrayOutputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.Hashtable;
//import java.util.Map;
//
//public class picsender extends AppCompatActivity {
//
//    Button btn_uplaoder;
//    private int PICK_IMAGE_REQUEST=1;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_picsender);
//        showFileChoser();
//    }
//
//    private void showFileChoser() {
//        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setType("image/*");
//        intent.putExtra("aspectX",1);
//        intent.putExtra("aspectY",1);
//        intent.putExtra("scale",true);
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//        startActivityForResult(intent,PICK_IMAGE_REQUEST);
//        Log.d("mine tag", "showFileChoser: ");
//
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data !=null && data.getData()!=null){
//
//            Uri filepath=data.getData();
//            try{
//                Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),filepath);
//                Bitmap lastbitmap=null;
//                lastbitmap=bitmap;
//
//                String image=getStringImage(lastbitmap);
//                Log.d("Imginstr", image);
//
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private String getStringImage(Bitmap bmp) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        byte[] imageBytes = baos.toByteArray();
//        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
//        return encodedImage;
//    }
//
//}
