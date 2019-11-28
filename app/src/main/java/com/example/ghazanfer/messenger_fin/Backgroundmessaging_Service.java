package com.example.ghazanfer.messenger_fin;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Backgroundmessaging_Service extends Service {

    public final IBinder binder=new localbinder();
    Handler x=new Handler();
    static String username;
    String login_unique_id;
    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor;
    OkHttpClient okHttpClient;


    Communicator comm;
    databaseHelper mdatabaseHelper;
    boolean unsendmessages=false;
    int m=0;
    int k=0;
    int count=0;
    boolean checkBool=true;
    Thread messageReceiverthr;

    public class localbinder extends Binder{
        Backgroundmessaging_Service getService(){
            return Backgroundmessaging_Service.this;
        }
    }

    public void setCallbacks(Communicator o_comm){//Orignal communicator coming from activity which implements communicator
        comm=o_comm;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        int mode= Activity.MODE_PRIVATE;
        mySharedPreferences=getSharedPreferences("MySharedPreference",mode);
        editor= mySharedPreferences.edit();
        username=mySharedPreferences.getString("username","null");
        login_unique_id=mySharedPreferences.getString("login_unique_id","null");

        Log.d("0002", "onCreate: Starting Background_sevice USername = "+username+" login_unique_id= "+login_unique_id);
        mdatabaseHelper=databaseHelper.getDatabaseHelper(this);
        Log.d("POO", "onCreate: AGAYAA");

        okHttpClient=okhttpSingleton.gethttpSingleton().getOkHttpClient();
//        getFriends();
        messageReceiver();

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //code moved to oncreate because oncreate only called once and doesnt reset connection on every time opening app
//        if(comm!=null && mySharedPreferences.getString("contacts_object",null)!=null)comm.updateList(null,null);
        Log.d("0004", "onStartCommand: Starting Background_sevice USername = "+username);
        return START_STICKY;

    }

//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        Log.d("mytaskremove", "onTaskRemoved: ");
//        messageReceiverthr.interrupt();
//        super.onTaskRemoved(rootIntent);
//    }


    private void messageReceiver(){
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                while(checkBool) {
                    try {
                        Thread.sleep(2000);
                        //getting current time in UTC
                        final String DATEFORMAT = "MMM dd, yyyy hh:mm:ss a";
                        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT, Locale.ENGLISH);
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String utcTime = sdf.format(new Date());

                        Log.d("myuserkeera", "username"+username+"  id:"+login_unique_id);
                        RequestBody requestBody;
                        if(comm!=null) {//if app is open
                            //continously send request to get messages
                            requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).
                                    addFormDataPart("special_key", okhttpSingleton.SPECIAL_ACCESS_KEY).
                                    addFormDataPart("operation", "getMessage").
                                    addFormDataPart("login_unique_id", login_unique_id).
                                    addFormDataPart("username", username).
                                    addFormDataPart("lastSeen", "Online").
                                    build();
                        }else{//if app is in background so not updating last seen in server
                            //continously send request to get messages
                            requestBody= new MultipartBody.Builder().setType(MultipartBody.FORM).
                                    addFormDataPart("special_key", okhttpSingleton.SPECIAL_ACCESS_KEY).
                                    addFormDataPart("operation", "getMessage").
                                    addFormDataPart("login_unique_id", login_unique_id).
                                    addFormDataPart("username", username).
                                    addFormDataPart("lastSeen","notOnline").
                                    build();

                        }
                        Request request = new Request.Builder().url(okhttpSingleton.MYURL).post(requestBody).build();
                        if(checkBool==false)break;
                        Response response = okHttpClient.newCall(request).execute();
                        String responseStr = response.body().string();
                        Log.d("myrespontStr", responseStr);
                        JSONObject receivedObject=new JSONObject(responseStr);
                        JSONArray receivedMessagesArray = new JSONArray(receivedObject.getString("messageArray"));
                        int arraylen = receivedMessagesArray.length();
                        for (int i = 0; i < arraylen; i++) {
                            JSONObject singleObj = receivedMessagesArray.getJSONObject(i);
                            if(singleObj.has("mediaContent")){
                                JSONObject mediaandmessage=new JSONObject();
                                mediaandmessage.put("mediaContent",singleObj.getString("mediaContent"));//media content is also a json Object
                                mediaandmessage.put("content", singleObj.getString("content"));//putting this so the databse content wher we put mediaContent is
                                // not treated as messages for more see contacts frag line 343
                                long id = mdatabaseHelper.addData(singleObj.getString("sender"), singleObj.getString("receiver"), mediaandmessage.toString(),
                                        "received", singleObj.getString("time"));
                                if (comm != null)
                                    comm.updateList("", singleObj.getString("sender"));
                            }
                            else {
                                JSONObject messageobj=new JSONObject();
                                messageobj.put("content", singleObj.getString("content"));
                                long id = mdatabaseHelper.addData(singleObj.getString("sender"), singleObj.getString("receiver"), messageobj.toString(),
                                        "received", singleObj.getString("time"));
                                Log.d("CHECKER!", "run: "+id);
                                if (comm != null)
                                    comm.updateList("", singleObj.getString("sender"));
                                if (comm != null)
                                    comm.updateMessageUi("Sendini", id);//used for messages received from other users
                                Thread.sleep(300);
                            }
                        }

                            JSONArray receivedStatusArray = new JSONArray(receivedObject.getString("receiveStatusArray"));
                            arraylen = receivedStatusArray.length();
                            for (int i = 0; i < arraylen; i++) {
                                JSONObject singleObj = receivedStatusArray.getJSONObject(i);
                                long id = singleObj.getLong("msg_id_android");
                                final boolean xy = mdatabaseHelper.updateReceived(id, "sendReceived");//The item we sent is received at the other end

                                //if (comm != null) comm.updateList("", singleObj.getString("sender"));
                                if (comm != null) comm.updateMessageUi("sendReceived", id);
                                if (comm != null) comm.contactListNotifyDataChange();
                                Thread.sleep(300);
                            }
                        if(receivedObject.getString("friendLastSeenBool").equals("True")) {//this is to check if the current user is online else this wont be sent here(will be false)

                            if (comm != null)
                                comm.setLastSeenArray(receivedObject.getString("friendLastSeenArray"));//For contacts Frag
                        }
                    } catch (IOException e) {
                        try {
                            Thread.sleep(6000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        messageReceiverthr=new Thread(runnable);
        messageReceiverthr.start();

    }


//    private void setup() throws IOException {
//
//
//            String serv_msg;
////            input=new ObjectInputStream(connection.getInputStream());
////            output=new ObjectOutputStream(connection.getOutputStream());
//            singleMessage jsonObj;
//            ObjectMapper mapper=new ObjectMapper();
////            sendMessage("!@##$% messaging connected-"+username,-1);
////            checker();
//            do
//            {
////               serv_msg= (String) input.readObject();
//                serv_msg="";
////                Log.i("ponka",serv_msg);
////                System.out.print(serv_msg);
//                if(serv_msg.contains("!@##$% ContactsRequest:")){
//                    serv_msg=serv_msg.replace("!@##$% ContactsRequest:","");
//                    String[] array=mapper.readValue(serv_msg, String[].class);
//                   // comm.contactRequest(array);
//                    continue;
//                }
//                if(serv_msg.contains("!@##$% username name:")){
//
//                    String name=serv_msg.replace("!@##$% username name:","");
//                    Log.d("namekarna", "setup: "+name);
//                    comm.setName(name);
//                }
//                jsonObj=mapper.readValue(serv_msg,singleMessage.class);
//                if(jsonObj.n>0 )
//                {
//                    //Putting contacts and their 1 message in shared preferance so other ui can get them and not passing in parameter so when in background
//
//                        editor.putString("contacts_object",serv_msg);
//                        editor.commit();
//                    //end
////                    if(comm !=null) comm.userListPopulator();
////                    dealUnsendMessages();
//                    continue;
//                }
//
//                if(jsonObj.n>0)
//                {
////                    dealUnsendMessages();
//                    continue;
//
//                }
//                if(jsonObj.receivedStatus==true)
//                {
//
//                    final boolean xy=mdatabaseHelper.updateReceived(jsonObj.msg_id,"sendReceived");//The item we sent is received at the other end
////                    if(comm !=null)comm.setText(null);
//
//                    continue;
//                }
//
//                mdatabaseHelper.addData(jsonObj.Sender,jsonObj.receiver,jsonObj.message,"received",jsonObj.date);
//                if(comm !=null)comm.updateList("",jsonObj.Sender);
////                if(comm !=null)comm.setText(null);
//
//                final String finalServ_msg = serv_msg;
//
//            }while (!serv_msg.equals("asda"));
//
//
//
//
//    }
    public void sendMessage(final String sender, final String receiver, final String content, final String time, final long id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("sender",sender);
                jsonObject.put("receiver",receiver);
                jsonObject.put("content",content);
                jsonObject.put("time",time);
                jsonObject.put("msg_id",id);
                RequestBody requestBody=new MultipartBody.Builder().setType(MultipartBody.FORM).
                        addFormDataPart("special_key",okhttpSingleton.SPECIAL_ACCESS_KEY).
                        addFormDataPart("operation","sendMessage").
                        addFormDataPart("login_unique_id",login_unique_id).
                        addFormDataPart("messageDetails",jsonObject.toString()).
                        addFormDataPart("username",username).
                        build();
                Request request=new Request.Builder().url(okhttpSingleton.MYURL).post(requestBody).build();

                Response response=okHttpClient.newCall(request).execute();

                    Log.d("messagesenderlog", response.body().string());
//                    if(comm !=null)comm.setMessageStatus(1);
                    //here will be updating that inserted message status to sent
                    mdatabaseHelper.updateSend(id, "Send");
                    if(comm !=null)comm.updateMessageUi("Send",id);
                    Thread.sleep(1000);

                } catch (IOException e) {
                    Log.d("sendmessageIOexception", "IO exception number: "+m++);
                    if(count==0){
                        count++;
                        dealUnsendMessages();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
//    void sendHeartBeat(String pulse) throws IOException {
//
////        output.writeObject(pulse);
//
//
//    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        checkBool=false;
    }


//    private void checker() {
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                while (true) {
//                    try {
//                        Thread.sleep(1500);//5000
//                        sendHeartBeat("!@##$% popip");
//                    }
//                    catch (IOException e) {
//                        Log.i("ponka","laari");
//                        e.printStackTrace();
//                        return;
//
//                    }
//                    catch (InterruptedException e) {
//                        Log.i("ponka","bhonk");
//
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
//    }


    public void dealUnsendMessagesoutside(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                dealUnsendMessages();
            }
        }).start();
    }
    private void dealUnsendMessages() {
        while(true) {
            Log.d("unsendmess being call", "dealUnsendMessages: called");
            Cursor cursor = mdatabaseHelper.getData("SELECT * FROM messages" + Backgroundmessaging_Service.username + " WHERE messageStatus = 'Not Send'");
            while (cursor.moveToNext()) {
                String receiver = cursor.getString(2);
                String messageToBeSend = cursor.getString(3);
                String date = cursor.getString(5);
                final long id = cursor.getLong(0);
                if (comm != null) {


                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("sender", username);
                        jsonObject.put("receiver", receiver);
                        jsonObject.put("content", messageToBeSend);
                        jsonObject.put("time", date);
                        jsonObject.put("msg_id", id);
                        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).
                                addFormDataPart("special_key", okhttpSingleton.SPECIAL_ACCESS_KEY).
                                addFormDataPart("operation", "sendMessage").
                                addFormDataPart("login_unique_id", login_unique_id).
                                addFormDataPart("messageDetails", jsonObject.toString()).
                                addFormDataPart("username", username).
                                build();
                        Request request = new Request.Builder().url(okhttpSingleton.MYURL).post(requestBody).build();

                        Response response = okHttpClient.newCall(request).execute();

                        Log.d("messagesenderlog", response.body().string());
                        if (comm != null) comm.setMessageStatus(1);
                        //here will be updating that inserted message status to sent
                        mdatabaseHelper.updateSend(id, "Send");
                        if (comm != null) comm.updateMessageUi("resend", id);
                        Thread.sleep(500);

                    } catch (IOException e) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        Log.d("resendmessageIO", "IO exception number: " + k++);
                        cursor = mdatabaseHelper.getData("SELECT * FROM messages" + Backgroundmessaging_Service.username + " WHERE messageStatus = 'Not Send'");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
        count = 0;

    }

    public void addFriendServerReq(final String searchText, int offset){//REtrieving the list which we could add
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).
                            addFormDataPart("special_key", okhttpSingleton.SPECIAL_ACCESS_KEY).
                            addFormDataPart("operation", "getAddFriendList").
                            addFormDataPart("login_unique_id", login_unique_id).
                            addFormDataPart("username", username).
                            addFormDataPart("offset","0").//HAVE TO IMPLEMENT THE OFFSET LOGIC
                            addFormDataPart("searchText",searchText).
                            build();
                        Request request = new Request.Builder().url(okhttpSingleton.MYURL).post(requestBody).build();
                        Response response = okHttpClient.newCall(request).execute();
                        String responseStr = response.body().string();
                        Log.d("addfriendresponsestr", responseStr);
                        JSONArray receivedArray = new JSONArray(responseStr);
                        int arraylen = receivedArray.length();
                        String[] username=new String[arraylen];
                        String[] fullname=new String[arraylen];
                        String[] status=new String[arraylen];
                        for (int i = 0; i < arraylen; i++) {
                            JSONObject singleObj = receivedArray.getJSONObject(i);
                            username[i]=singleObj.getString("username");
                            fullname[i]=singleObj.getString("fullname");
                            status[i]=singleObj.getString("status");
                        }
                        if(comm!=null)comm.contactRequest(username,fullname,status);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


    }
    public void sendFriendRequest(final String requestedUsername){//Sending the request to a particular user

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).
                            addFormDataPart("special_key", okhttpSingleton.SPECIAL_ACCESS_KEY).
                            addFormDataPart("operation", "sendFriendRequest").
                            addFormDataPart("login_unique_id", login_unique_id).
                            addFormDataPart("username", username).
                            addFormDataPart("requestedUsername",requestedUsername).
                            build();
                    Request request = new Request.Builder().url(okhttpSingleton.MYURL).post(requestBody).build();
                    Response response = okHttpClient.newCall(request).execute();
                    String responseStr = response.body().string();
                    Log.d("addfriendresponsestr", responseStr);

                    //if(comm!=null)comm.contactRequest(username,fullname);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    public void cancelSendFriendRequest(final String requestedUsername) {//canceling the send request to a particular user
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).
                            addFormDataPart("special_key", okhttpSingleton.SPECIAL_ACCESS_KEY).
                            addFormDataPart("operation", "cancelSendFriendRequest").
                            addFormDataPart("login_unique_id", login_unique_id).
                            addFormDataPart("username", username).
                            addFormDataPart("requestedUsername",requestedUsername).
                            build();
                    Request request = new Request.Builder().url(okhttpSingleton.MYURL).post(requestBody).build();
                    Response response = okHttpClient.newCall(request).execute();
                    String responseStr = response.body().string();
                    Log.d("addfriendresponsestr", responseStr);

                    //if(comm!=null)comm.contactRequest(username,fullname);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void acceptFriendRequest(final String requestedUsername){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).
                            addFormDataPart("special_key", okhttpSingleton.SPECIAL_ACCESS_KEY).
                            addFormDataPart("operation", "acceptSendFriendRequest").
                            addFormDataPart("login_unique_id", login_unique_id).
                            addFormDataPart("username", username).
                            addFormDataPart("requestedUsername",requestedUsername).
                            build();
                    Request request = new Request.Builder().url(okhttpSingleton.MYURL).post(requestBody).build();
                    Response response = okHttpClient.newCall(request).execute();
                    String responseStr = response.body().string();
                    Log.d("addfriendresponsestr", responseStr);

                    //if(comm!=null)comm.contactRequest(username,fullname);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void sendFile(File myFile, String mime_type, String receiver, String utctime, final long id){

        login_unique_id = mySharedPreferences.getString("login_unique_id", "null");

        final OkHttpClient client = okhttpSingleton.gethttpSingleton().getOkHttpClient();
        final MediaType MEDIA_TYPE_PDF = MediaType.parse("application/pdf");//".pdf",".apk",".mp3",".docx",".pptx",".xlsx"}
        final MediaType MEDIA_TYPE_APK = MediaType.parse("application/vnd.android.package-archive");
        final MediaType MEDIA_TYPE_MP3 = MediaType.parse("audio/mpeg");
        final MediaType MEDIA_TYPE_DOCX = MediaType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        final MediaType MEDIA_TYPE_PPTX = MediaType.parse("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        final MediaType MEDIA_TYPE_XLSX = MediaType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

        MediaType sendMediaType=null;
        if(mime_type.equals(".pdf"))sendMediaType=MEDIA_TYPE_PDF;
        if(mime_type.equals(".apk"))sendMediaType=MEDIA_TYPE_APK;
        if(mime_type.equals(".mp3"))sendMediaType=MEDIA_TYPE_MP3;
        if(mime_type.equals(".docx"))sendMediaType=MEDIA_TYPE_DOCX;
        if(mime_type.equals(".pptx"))sendMediaType=MEDIA_TYPE_PPTX;
        if(mime_type.equals(".xlsx"))sendMediaType=MEDIA_TYPE_XLSX;
        if(mime_type.equals(".png"))sendMediaType=MEDIA_TYPE_PNG;
        Log.d("yolocheck", "sendFile: "+receiver);

        RequestBody requestBody = new MultipartBody.Builder().
                setType(MultipartBody.FORM).
                addFormDataPart("special_key", okhttpSingleton.SPECIAL_ACCESS_KEY).
                addFormDataPart("operation", "mediaUpload").
                addFormDataPart("username", username).
                addFormDataPart("login_unique_id", login_unique_id).
                addFormDataPart("receiver",receiver).
                addFormDataPart("time",utctime).
                addFormDataPart("msg_id",id+"").
                addFormDataPart("mediaUploadFile", myFile.getName(), RequestBody.create(sendMediaType, myFile)).
                build();
        final Request request = new Request.Builder()
                .url(okhttpSingleton.MYURL)
                .post(requestBody)
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    final String responseStr = response.body().string();
                    Log.d("imagecallresp", responseStr);
                    JSONObject obj=new JSONObject(responseStr);
                    Log.d("checkerr", "run: "+obj.getString("status"));
                    if(obj.getString("status").equals("Success")){
                        mdatabaseHelper.updateSend(id, "Send");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }



    }
