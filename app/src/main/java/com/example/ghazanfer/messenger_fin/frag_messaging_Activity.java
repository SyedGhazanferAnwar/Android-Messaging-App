package com.example.ghazanfer.messenger_fin;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import id.zelory.compressor.Compressor;

public class frag_messaging_Activity extends AppCompatActivity  implements Communicator{


//    String xoxo="";
//    Button loaderact;

    Messaging_UI_FRAG uiFrag;
    AddFriends addFriends;
    contacts_frag contactsFrag;
    FragmentTransaction transaction;
    private Backgroundmessaging_Service myService;
    private boolean bound = false;
    SharedPreferences mySharedPreferences;
    AppBarLayout appBarLayout;
    settings msettings;
    File compressedImageFile;
    final int CAMERA_PIC_TAKEN=165;
    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE=100;
    Uri filepathuri;
    File fileToBeSend;
    static String username;
    static String receiverForImageSending;
    //FOR BACKGROUNDMESSAGING_SERVICE BINDING
    @Override
    protected void onStart() {
        super.onStart();
        Intent backgroundService=new Intent(getApplicationContext(),Backgroundmessaging_Service.class);
        startService(backgroundService);
        Log.d("0001", "onStart: Starting frag_messaging_Actitivity");
        bindService(backgroundService,serviceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(bound){
            myService.setCallbacks(null);
            unbindService(serviceConnection);
            bound=false;
        }
    }

    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Backgroundmessaging_Service.localbinder binder= (Backgroundmessaging_Service.localbinder) service;
            myService=binder.getService();
            bound=true;
            myService.setCallbacks(frag_messaging_Activity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound=false;
        }
    };

    //    //FOR BACKGROUNDMESSAGING_SERVICE ENDSS
    FragmentManager manager;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag_messaging);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        appBarLayout= (AppBarLayout) findViewById(R.id.appbar);
        msettings =new settings();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("144587", "onTabSelected1: "+tab.getText());
                if(tab.getText().equals("Chat")){
                    //contactsFrag.getFriends(1);
                    contactsFrag.hideKeyboard(frag_messaging_Activity.this);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        uiFrag =new Messaging_UI_FRAG();
        addFriends=new AddFriends();
        contactsFrag=new contacts_frag();
        manager=getSupportFragmentManager();

        int mode= Activity.MODE_PRIVATE;
        mySharedPreferences=getSharedPreferences("MySharedPreference",mode);

    }

    @Override
    public void updateMessageUi(String update,long id) {
        Fragment f=manager.findFragmentByTag("uiFrag");
        if(f !=null){

            if(f.isVisible()) uiFrag.updateMessageUi(update,id);
        }

    }

    @Override
    public void setName(String name) {
        addFriends.setName(name);
    }

    @Override
    public void sendMessage(String sender,String receiver,String content,String time,long id) {
        myService.sendMessage(sender,receiver,content,time,id);
    }

    @Override
    public void messageOpener(String receiver) {


        if(!uiFrag.isAdded()) {
            uiFrag.setReceiver(receiver);
            transaction=manager.beginTransaction();
            transaction.add(R.id.main_content, uiFrag, "uiFrag");
            transaction.remove(contactsFrag);
            transaction.addToBackStack("contactsFrag");
            transaction.commit();
            myService.dealUnsendMessagesoutside();

            appBarLayout.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    public void updateList(String LastMessage,String LastMessageContact) {
       contactsFrag.updateList(LastMessageContact);
    }

    @Override
    public void setMessageStatus(int status) {
    }

    @Override
    public void resetConnection() {

        Intent intent=new Intent(frag_messaging_Activity.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void contactRequest(String[] contacts,String[] fullname,String[] status) {
        addFriends.contactRequestHandle(contacts,fullname,status);
    }

    @Override
    public void addFriendServerReq(String searchText,int offset) {
        myService.addFriendServerReq(searchText,offset);

    }

    @Override
    public void sendFriendRequest(String requestedUsername) {
        myService.sendFriendRequest(requestedUsername);
    }
    @Override
    public void cancelSendFriendRequest(String requestedUsername) {
        myService.cancelSendFriendRequest(requestedUsername);
    }

    public void acceptFriendRequest(String requestedUsername){
        myService.acceptFriendRequest(requestedUsername);
    }

    @Override
    public void getFriends(int override) {
        contactsFrag.getFriends(override);
    }

    @Override
    public void contactListNotifyDataChange() {
        contactsFrag.notify_DataSetChanged();
    }

    @Override
    public void setLastSeenArray(String x) {
        contactsFrag.setLastSeenArray(x);
        if(uiFrag.isAdded()){
            try {
                JSONArray lastSeenJson=new JSONArray(x);
                for(int i=0;i<lastSeenJson.length();i++){
                    JSONObject singleObj = lastSeenJson.getJSONObject(i);
                    String username_show=singleObj.getString("username");
                    String lastSeen=singleObj.getString("lastSeen");
                    if(username_show.equals(uiFrag.receiver)){
                        uiFrag.updateLastSeenStatus(lastSeen);
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //For image cropping is conditional on button press
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filepathuri = result.getUri();
                loadImageAfterCrop();//image cropped

            }
        }
            //image cropping ends

        if(resultCode == RESULT_OK && requestCode==CAMERA_PIC_TAKEN){//CAMERA_PIC_TAKEN representing both gallery pickup and image capture
            //receiverForImageSending= getIntent().getStringExtra("receiver");
            filepathuri = data.getData();
            //Log.d("receivertextreceive", "onActivityResult: thank you "+receiverForImageSending);
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
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    }
                    else {
                        // Permission has already been granted
                        Log.d("mtripermission", "onActivityResult: PERMISSION GRANTED");
                            prepareImage();

                    }
                }

            }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }
    ImageView imageviewcenterbig;

    public void loadImageAfterCrop(){
        fileToBeSend = new File(filepathuri.getPath());
        Bitmap bm=dealRotation(filepathuri.getPath());
        imageviewcenterbig.setImageBitmap(bm);

    }
    public void prepareImage(){
            fileToBeSend = new File(getPath(filepathuri));
//            CropImage.activity(filepathuri)
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .start(this);
            final Dialog dialog=new Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.image_prepare);
            LinearLayout sendButtonCircle=dialog.findViewById(R.id.sendButtonCircle);
            imageviewcenterbig=dialog.findViewById(R.id.imageviewcenterbig);
            Button cancelThisLayout=dialog.findViewById(R.id.cancelThisLayouts);
            Button imageCropperOpen=dialog.findViewById(R.id.imageCropperOpen);
            Bitmap bm=dealRotation(getPath(filepathuri));
            imageviewcenterbig.setImageBitmap(bm);


            cancelThisLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            imageCropperOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(frag_messaging_Activity.this, "Open Image cropper", Toast.LENGTH_SHORT).show();
                    CropImage.activity(filepathuri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(frag_messaging_Activity.this);
                }
            });
            sendButtonCircle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        compressedImageFile = new Compressor(frag_messaging_Activity.this).compressToFile(fileToBeSend);
                        sendImageFile(compressedImageFile);

                        //call functions to show this image in ui

                        dialog.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            dialog.show();

    }

    public Bitmap dealRotation(String picturePath){
        Bitmap loadedBitmap = BitmapFactory.decodeFile(picturePath);

        ExifInterface exif = null;
        try {
            File pictureFile = new File(picturePath);
            exif = new ExifInterface(pictureFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = ExifInterface.ORIENTATION_NORMAL;

        if (exif != null)
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                loadedBitmap = rotateBitmap(loadedBitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                loadedBitmap = rotateBitmap(loadedBitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                loadedBitmap = rotateBitmap(loadedBitmap, 270);
                break;
        }
        return loadedBitmap;
    }
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    public void sendFile(File file,String mime_type,String receiver) {
        Log.d("fileSent", "sendFile: ");

        //calc time and conveting to utc
        final String DATEFORMAT = "MMM dd, yyyy hh:mm:ss a";
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT, Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcTime = sdf.format(new Date());//sdf.format gives us UTC time after setting .getTimeZone to UTC
        databaseHelper mdatabaseHelper=databaseHelper.getDatabaseHelper(frag_messaging_Activity.this);
        JSONObject messageobj=new JSONObject();
        try {
            messageobj.put("content","");//empty to differentiate btw messages and media
            messageobj.put("mediaContent",file.getAbsolutePath());
            long id=mdatabaseHelper.addData(username,receiver,messageobj.toString(),"Not Send",utcTime);

    //        updateMessageUi("Sendini",id);
    //        comm.updateList("",receiver);

            myService.sendFile(file,mime_type,receiver,utcTime,id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendImageFile(File file) {
        //calc time and conveting to utc
        final String DATEFORMAT = "MMM dd, yyyy hh:mm:ss a";
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT, Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcTime = sdf.format(new Date());//sdf.format gives us UTC time after setting .getTimeZone to UTC
        databaseHelper mdatabaseHelper = databaseHelper.getDatabaseHelper(frag_messaging_Activity.this);
        JSONObject messageobj = new JSONObject();
        try {
            messageobj.put("content", "");//empty to differentiate btw messages and media
            messageobj.put("mediaContent", file.getAbsolutePath());
            long id = mdatabaseHelper.addData(username, receiverForImageSending, messageobj.toString(), "Not Send", utcTime);

            myService.sendFile(file,"png",receiverForImageSending,utcTime,id);//check thiss bhundd

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    @Override
    public void fileSentNotify() {
        uiFrag.fileSentNotify();
    }



    ///SWIPE TABS IMPLEMNETATION



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {



        if (item.getTitle().equals("Setting")) {
            Toast.makeText(this, "Setting PRESSED", Toast.LENGTH_SHORT).show();
//            if(!msettings.isAdded()) {
//                transaction=manager.beginTransaction();
//                transaction.add(R.id.main_content, msettings, "settingsFragment");
//                transaction.remove(contactsFrag);
//                transaction.addToBackStack("contactsFrag");
//                transaction.commit();
//                appBarLayout.setVisibility(View.INVISIBLE);
//
//            }
            Intent i=new Intent(this,settings.class);
            startActivity(i);
        }
        if (item.getTitle().equals("Logout")) {

            Intent backgroundService=new Intent(getApplicationContext(),Backgroundmessaging_Service.class);
            stopService(backgroundService);
            databaseHelper.closeDatabaseHelper();
            //contactsFrag.setCredentials(null,null);
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putString("LogedInInfo", "LogedOut");
            editor.putString("password", "");
            editor.putString("username", "");
            editor.putString("profileImageUrl",null);
            editor.putString("ContactsList","no contacts");
            editor.commit();
            Log.d("mytaskr00e00", "onTaskRemoved: "+mySharedPreferences.getString("ContactsList",null));

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);

    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tabbed, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            if(position==0){
                return contactsFrag;
            }
            if(position==1)return addFriends;
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

}


//when the message is not send and phone app is reinstalled the unsend message is not send ..applied a approach when calling undsend on opening uifrag
//total lines of code using 2620