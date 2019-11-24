package com.example.ghazanfer.messenger_fin;

import android.app.Activity;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class Messaging_UI_FRAG extends Fragment {
    RecyclerView recyclerView;
    Button send;
    EditText sendText;
    Handler x=new Handler();
    static String username;
    String receiver;
    int f=0;
    Communicator comm;
    recyclerAdapter adapter;
    databaseHelper mdatabaseHelper;
    ArrayList<singleMessage2> list_objects;
    LinearLayout backbtnlayout_chat;
    FragmentManager manager;
    TextView profile_name;
    ImageView profile_pic;
    View view;
    Button attachment;
    TextView lastSeenStatustxtv;
    CustomBottomSheetDialogFragment bottomSheetDialogFragment;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        manager=getActivity().getSupportFragmentManager();

        view=inflater.inflate(R.layout.fragment_messaging__ui__frag, container, false);
        recyclerView= (RecyclerView) view.findViewById(R.id.reyclerview_message_list);
        profile_name= (TextView) view.findViewById(R.id.profile_name);
        profile_pic=view.findViewById(R.id.profile_pic);
        profile_name.setText(receiver);
        lastSeenStatustxtv= (TextView) view.findViewById(R.id.lastSeenStatustxtv);
        String imageUrl=contacts_frag.thisProfileImageUrl.get(contacts_frag.thisfriends.indexOf(receiver));
        Glide.with(this).
                load(okhttpSingleton.MYBASEURL+imageUrl).
                apply(new RequestOptions().placeholder(R.drawable.placeholder).error(R.drawable.placeholder)).
                into(profile_pic);
        attachment=view.findViewById(R.id.attachment);
        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialogFragment = new CustomBottomSheetDialogFragment();
                bottomSheetDialogFragment.receiver=receiver;





                //show it
                bottomSheetDialogFragment.show(getFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

        mdatabaseHelper=databaseHelper.getDatabaseHelper(getActivity());
        Cursor cursor=mdatabaseHelper.getData("SELECT * FROM messages"+Backgroundmessaging_Service.username+" WHERE (Receiver='"+receiver+"' AND Sender='"+username+"') OR (SENDER='"+receiver+"' AND Receiver='"+username+"') ORDER BY ID");
        list_objects=new ArrayList<>();
        while(cursor.moveToNext())
        {
            try {
                JSONObject mediaAndMessages=new JSONObject(cursor.getString(3));//this is jsonObject which contains messges or message and media
                Log.d("hatrhihai", "onCreateView: "+cursor.getString(3));
                if(mediaAndMessages.has("mediaContent")){//mediaContent is also object

                    singleMessage2 obj = new singleMessage2();
                    boolean isMediaReceived=checkMediaReceive(mediaAndMessages.getString("mediaContent"));//checking if receive so it will have json object in database else will have a simple local path of file
                    if(isMediaReceived) {
                        JSONObject jsonMedia = new JSONObject(mediaAndMessages.getString("mediaContent"));
                        String fileName = jsonMedia.getString("fileName");
                        String extention = fileName.substring(fileName.lastIndexOf("."));
                        Log.d("extentioncheck", "onCreateView: " + extention);

                        obj.mediaName = fileName;
                        //obj.mediaPath = jsonMedia.getString("fileLocation");
                        obj.mediaSize = jsonMedia.getString("fileSize");
                        obj.mediaType = extention;
                    }else{//media is the one which i sent
                        String pathToLocalMedia=mediaAndMessages.getString("mediaContent");
                        String extention=pathToLocalMedia.substring(pathToLocalMedia.lastIndexOf("."));

                        File file=new File(pathToLocalMedia);
                        obj.mediaType=extention;
                        obj.mediaName=file.getName();
                        obj.mediaSize=(file.length()/1048576)+"";
                        obj.mediaPath=pathToLocalMedia;

                    }
                    obj.Sender = cursor.getString(1);//sender
                    obj.receiver = cursor.getString(2);//receiver
                    obj.status = cursor.getString(4);//status
                    String dateStr = cursor.getString(5);
                    String[] dateArr = dateConversion(dateStr);
                    obj.date = dateArr[0];
                    obj.time = dateArr[1];
                    Log.d("hello1med", "Mediamsg is :  " + obj.mediaName + "   " + obj.status);
                    list_objects.add(obj);


                }else {

                    singleMessage2 obj = new singleMessage2();
                    obj.Sender = cursor.getString(1);//sender
                    obj.message =mediaAndMessages.getString("content");//message
                    obj.receiver = cursor.getString(2);//receiver
                    obj.status = cursor.getString(4);//status
                    obj.mediaType=null;
                    String dateStr = cursor.getString(5);
                    String[] dateArr = dateConversion(dateStr);
                    obj.date = dateArr[0];
                    obj.time = dateArr[1];


                    Log.d("hello1", "msg is :  " + obj.message + "   " + obj.status);
                    list_objects.add(obj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //username=mySharedPreferences.getString("username",null); Not using this could cause error in future


        adapter=new recyclerAdapter(getActivity(),list_objects,username);
        recyclerView.setAdapter(adapter);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        return view;
    }

    public boolean checkMediaReceive(String json){//parsing json if parsed it is received and is a json object and if not it is simple local file path
        try {
            JSONObject obj=new JSONObject(json);
            return true;
        } catch (JSONException e) {
            //e.printStackTrace();
            return false;
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        send= (Button) getActivity().findViewById(R.id.sendbutton);
        sendText= (EditText) getActivity().findViewById(R.id.editText);
        backbtnlayout_chat= (LinearLayout) getActivity().findViewById(R.id.backbtnlayout_chat);
        backbtnlayout_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                manager.popBackStackImmediate();

            }
        });
        comm= (Communicator) getActivity();
        mdatabaseHelper=databaseHelper.getDatabaseHelper(getActivity());
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String DATEFORMAT = "MMM dd, yyyy hh:mm:ss a";
                SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT,Locale.ENGLISH);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String utcTime = sdf.format(new Date());//sdf.format gives us UTC time after setting .getTimeZone to UTC

                String messageToBeSend=sendText.getText().toString();
                if(messageToBeSend.equals(""))return;
                JSONObject messageobj=new JSONObject();
                try {
                    messageobj.put("content", messageToBeSend);
                    long id=mdatabaseHelper.addData(username,receiver,messageobj.toString(),"Not Send",utcTime);
                    updateMessageUi("Sendini",id);
                    comm.updateList("",receiver);
                    sendText.setText("");
                    comm.sendMessage(username,receiver,messageToBeSend,utcTime,id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }



    public void updateLastSeenStatus(final String status){
        x.post(new Runnable() {
            @Override
            public void run() {
                if (status.equals("Online")) {
                    lastSeenStatustxtv.setText(status);
                }
                else {
                    try {
                        SimpleDateFormat sdfDay = new SimpleDateFormat("E", Locale.ENGLISH);//dd/MM/yyyy
                        Date currentDate = new Date();
                        String currentStrDay = sdfDay.format(currentDate);
                        SimpleDateFormat obtainedFormat=new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);//this is the format of status obtained

                        obtainedFormat.setTimeZone(TimeZone.getTimeZone("UTC"));//changing the timezone beacuse status is in UTC
                        Date obtainedDate = obtainedFormat.parse(status);//This is to parse the status to date
                        String obtainedStrDay = sdfDay.format(obtainedDate);

                        if (obtainedStrDay.equals(currentStrDay)) {
                            String time = new SimpleDateFormat("h:mm a", Locale.ENGLISH).format(obtainedDate);
                            lastSeenStatustxtv.setText("Last seen today at "+time);
                        } else {
                            String dateTime = new SimpleDateFormat("E h:mm a", Locale.ENGLISH).format(obtainedDate);//E is for day ex:Fri
                            lastSeenStatustxtv.setText("Last seen " + dateTime);
                        }
                    }catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                }
        });
    }



    void updateMessageUi(String update,long id){
        mdatabaseHelper=databaseHelper.getDatabaseHelper(getActivity());
        if(update.equals("Sendini")){//also used for messages received from other users
            String query="SELECT * FROM messages"+Backgroundmessaging_Service.username+" WHERE ID="+id;
            Cursor cursor=mdatabaseHelper.getData(query);
            singleMessage2 obj=new singleMessage2();
            while(cursor.moveToNext()){

                try {
                    JSONObject mediaAndMessages=new JSONObject(cursor.getString(3));//this is jsonObject which contains messges or message and media
                    if(mediaAndMessages.has("mediaContent")){



                    }else {

                        obj.Sender = cursor.getString(1);//sender
                        obj.message =mediaAndMessages.getString("content");//message
                        obj.receiver = cursor.getString(2);//receiver
                        obj.status = cursor.getString(4);//status
                        obj.id=id;

                        Log.d("updatemessageui", "updateMessageUi: "+obj.Sender);
                        String dateStr = cursor.getString(5);
                        String[] dateArr=dateConversion(dateStr);
                        obj.date=dateArr[0];
                        obj.time=dateArr[1];
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            recyclerAdapter.list.add(obj);
            final int itemindex=recyclerAdapter.list.indexOf(obj);
            x.post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyItemInserted(itemindex);
                    recyclerView.smoothScrollToPosition(recyclerAdapter.list.size()-1);
                }
            });
        }
        else if(update.equals("Send")){
            int i;
            for(i=1;i<=recyclerAdapter.list.size();i++){//searching from last because it is most probably from the last messages
                singleMessage2 obj=recyclerAdapter.list.get(recyclerAdapter.list.size()-i);
                if(obj.id==id){
                    obj.status="Send";
                    break;
                }
            }
            final int specindex=recyclerAdapter.list.size()-i;;

            x.post(new Runnable() {
                @Override
                public void run() {
                     adapter.notifyItemChanged(specindex);
                    recyclerView.smoothScrollToPosition(recyclerAdapter.list.size()-1);
                }
            });
        }
        else if(update.equals("resend")){//resend and also used when received on other side (Double Tick)
            int i;
            for(i=1;i<=recyclerAdapter.list.size();i++){//searching from last because it is most probably from the last messages
                singleMessage2 obj=recyclerAdapter.list.get(recyclerAdapter.list.size()-i);
                if(obj.id==id){
                    obj.status="Send";
                    break;
                }
            }
            final int specindex=recyclerAdapter.list.size()-i;;

            x.post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyItemChanged(specindex);
                }
            });
        }
        else if(update.equals("sendReceived")){


            int i;
            for(i=1;i<=recyclerAdapter.list.size();i++){//searching from last because it is most probably from the last messages
                singleMessage2 obj=recyclerAdapter.list.get(recyclerAdapter.list.size()-i);
                if(obj.id==id){
                    obj.status="sendReceived";
                    break;
                }
            }
            final int specindex=recyclerAdapter.list.size()-i;;

            x.post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyItemChanged(specindex);
                }
            });

        }
    }

    public void fileSentNotify(){

        x.post(new Runnable() {
            @Override
            public void run() {
                bottomSheetDialogFragment.dismiss();
            }
        });

    }


    public void setReceiver(String x)
    {
        receiver=x;
    }



    public String[] dateConversion(String dateStr){
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        df.setTimeZone(TimeZone.getDefault());
        SimpleDateFormat newdf=new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        SimpleDateFormat new2df=new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        String formatteddate=newdf.format(date);
        String formattedtime=new2df.format(date);

        String[] mreturn=new String[2];
        mreturn[0]=formatteddate;
        mreturn[1]=formattedtime;
        return mreturn;



    }
}
