package com.example.ghazanfer.messenger_fin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.Inflater;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;





///THE CONTENT ARRAY IS NOT USED ONLY FRIENDS ARRAY IS USED




public class contacts_frag extends Fragment {

//    String[] thisfriends={"asda","sada"};
//    String[] thiscontent={"asda","sada"};
    public static ArrayList<String> thisfriends;//=new ArrayList<>();
    ArrayList<String> thiscontent;//=new ArrayList<>();
    public static ArrayList<String> thisProfileImageUrl;
    String[] thisLastSeen;
//   int status=1;
//    String lastMessage="";
//    String lastMessageContact="";
    Communicator comm;
    customAdapter customAdapterobj;
    ListView lv;
    Handler x=new Handler();
    public databaseHelper mdatabaseHelper;
    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor;
    String username_global;

    @Override
    public void onStart() {
        super.onStart();
        AppBarLayout appBarLayout;
        appBarLayout= (AppBarLayout) getActivity().findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.VISIBLE);


    }

    public contacts_frag()
    {
        thisLastSeen=new String[100];
        for(int i=0;i<100;i++)thisLastSeen[i]="hello";
       // mdatabaseHelper.getData("SELECT * FROM messages");


    }
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      View view= inflater.inflate(R.layout.fragment_contacts_frag, container, false);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

      lv= (ListView) view.findViewById(R.id.myListView);
        int mode= Activity.MODE_PRIVATE;
        mySharedPreferences=this.getActivity().getSharedPreferences("MySharedPreference",mode);
        editor=mySharedPreferences.edit();
        username_global=mySharedPreferences.getString("username",null);

        comm=(Communicator) getActivity();
      customAdapterobj=new customAdapter();


        lv.setAdapter(customAdapterobj);
      lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              comm.messageOpener(thisfriends.get(position));
          }
      });
      thisfriends=null;
      thiscontent=null;
      thisProfileImageUrl=null;
      getFriends(0);
      return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setLastSeenArray(String x){
        try {
            JSONArray lastSeenJson=new JSONArray(x);
            for(int i=0;i<lastSeenJson.length();i++){
                JSONObject singleObj = lastSeenJson.getJSONObject(i);
                String username_show=singleObj.getString("username");
                String lastSeen=singleObj.getString("lastSeen");
                thisLastSeen[thisfriends.indexOf(username_show)]=lastSeen; //Suspicious
            }
            notify_DataSetChanged();
            Log.d("ppppti", "setLastSeenArray: "+thisLastSeen[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void setCredentials(String[] friends, String[] content,String[] profileImageUrl) {
//        thiscontent=content;
//        thisfriends=friends;
        if (friends == null && content == null) {
            thisfriends.clear();
            thiscontent.clear();
            thisProfileImageUrl.clear();
        }
        else{
        thisfriends=new ArrayList<>(Arrays.asList(friends));
        thiscontent=new ArrayList<>(Arrays.asList(content));
        thisProfileImageUrl=new ArrayList<>(Arrays.asList(profileImageUrl));

        }
    }

    public void updateList(final String lastMessageContact)//some probelm with this func
    {
        if(lastMessageContact==null)return;
        int index=thisfriends.indexOf(lastMessageContact);//causing null pointer this statement
        int counter=0;
        for(int i=index-1;i>=0;i--)
        {
            thisfriends.set(index-(index-1-i),thisfriends.get(i));//counter=index-(index-i-1)
            //counter++;
        }
        if(index>0) thisfriends.set(0,lastMessageContact);

        notify_DataSetChanged();
    }

    public void notify_DataSetChanged(){
        x.post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getActivity(), "op "+lastMessageContact, Toast.LENGTH_SHORT).show();
                customAdapterobj.notifyDataSetChanged();
            }
        });
    }


    public void getFriends(int override){
        if(override!=1) {//override 1 means to skip the getting from my sharredPreferences
            try {
                String mystr = mySharedPreferences.getString("ContactsList", null);
                Log.d("testyuio", "getFriends: " + mystr);
                if (mystr != null) {
                    if (!mystr.equals("no contacts")) {
                        JSONArray receivedArray = new JSONArray(mystr);
                        int arraylen = receivedArray.length();
                        String[] friends = new String[arraylen];
                        String[] content = new String[arraylen];
                        String[] profileImageUrl=new String[arraylen];
                        for (int i = 0; i < arraylen; i++) {
                            JSONObject singleItem = receivedArray.getJSONObject(i);
                            friends[i] = singleItem.getString("friendName");
                            content[i] = singleItem.getString("message");
                            profileImageUrl[i]=singleItem.getString("profileImageUrl");
                        }
                        setCredentials(friends, content,profileImageUrl);
                        notify_DataSetChanged();
                        return;
                    }
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        String login_unique_id=mySharedPreferences.getString("login_unique_id",null);;
        final RequestBody requestBodygetcon=new MultipartBody.Builder().
                setType(MultipartBody.FORM).
                addFormDataPart("special_key",okhttpSingleton.SPECIAL_ACCESS_KEY).
                addFormDataPart("operation","getContacts").
                addFormDataPart("username",username_global).
                addFormDataPart("login_unique_id",login_unique_id).
                build();

        final Request requestgetcon=new Request.Builder().url(okhttpSingleton.MYURL).post(requestBodygetcon).build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = okhttpSingleton.gethttpSingleton().getOkHttpClient();
                    final Response response = okHttpClient.newCall(requestgetcon).execute();
                    final String mystr = response.body().string();
                    Log.d("chu0re", mystr);
                    editor.putString("ContactsList",mystr);
                    editor.commit();
                    JSONArray receivedArray = new JSONArray(mystr);
                    int arraylen = receivedArray.length();
                    String[] friends = new String[arraylen];
                    String[] content = new String[arraylen];
                    String[] profileImageUrl=new String[arraylen];

                    for (int i = 0; i < arraylen; i++) {
                        JSONObject singleItem = receivedArray.getJSONObject(i);
                        friends[i] = singleItem.getString("friendName");
                        content[i] = singleItem.getString("message");
                        profileImageUrl[i]=singleItem.getString("profileImageUrl");
                    }
                    setCredentials(friends,content,profileImageUrl);
                    notify_DataSetChanged();



                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }}).start();

    }


    public void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    class customAdapter extends BaseAdapter
    {
        customAdapter(){
            mdatabaseHelper=databaseHelper.getDatabaseHelper(getActivity());
        }
       /*


        WAS HERE FOLLOWING THE VIDEO FOR LIST VIEW THE ARRAYS OF MESSAGES AND FRIENDS ARE COMMING FROM SERVER IN JSON AND THEN FROM MESSAGING
        FRAG TO FRAG_MESSAGING_ACTIVITY TO HERE USING SETCREDENTIALS METHOD
        */

        //((BaseAdapter) ((ListView)findViewById(R.id.conv_list)).getAdapter()).notif‌​yDataSetChanged();


        @Override
        public int getCount() {
            if(thisfriends!=null)
            return thisfriends.size();
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            if(lastMessage.compareTo("")!=0)
//            {
//                if(thisfriends[position].compareTo(lastMessageContact)==0)thiscontent[position]=lastMessage;
//            }
            convertView= getActivity().getLayoutInflater().inflate(R.layout.custom_layout_singleitem,null);
            ImageView profile= (ImageView) convertView.findViewById(R.id.profilePic);
            Drawable drawable;
            Log.d("yaah habibi", "getView: "+thisProfileImageUrl.get(position));
            Glide.with(contacts_frag.this).
                    load(okhttpSingleton.MYBASEURL+thisProfileImageUrl.get(position)).
                    apply(new RequestOptions().placeholder(R.drawable.placeholder).error(R.drawable.placeholder)).
                    into(profile);

            ImageView greenCircleImage= (ImageView) convertView.findViewById(R.id.greenCircleImage);
            TextView contactName= (TextView) convertView.findViewById(R.id.ContactName);
            TextView contact_last_msg= (TextView) convertView.findViewById(R.id.Contact_last_msg);
            ImageView tick= (ImageView) convertView.findViewById(R.id.tick);
            Cursor cursor=mdatabaseHelper.getData("SELECT * FROM messages"+username_global+" WHERE (Receiver= '"+thisfriends.get(position)+"' AND Sender='"+Messaging_UI_FRAG.username+"') OR (Sender= '"+thisfriends.get(position) +"' AND Receiver='"+Messaging_UI_FRAG.username+"') ORDER BY ID DESC LIMIT 1");
            contactName.setText(thisfriends.get(position));
            if(thisLastSeen[position].equals("Online"))greenCircleImage.setBackgroundResource(R.drawable.circle_shape_green);
            else greenCircleImage.setBackgroundResource(R.drawable.circle_shape_grey);
            if(cursor==null){
                Log.d("TENTIONNH", "getView: YH WALA CURSOR NULL HOGYA");
            }
            while(cursor.moveToNext())//error line null on cursor.movetonext  java.lang.NullPointerException: Attempt to invoke interface method 'boolean android.database.Cursor.moveToNext()' on a null object reference
            {
                try {
                    JSONObject mediaAndMessages=new JSONObject(cursor.getString(3));//this is jsonObject which contains messges or message and media
                    if(mediaAndMessages.has("mediaContent")){//media Content is also json object

                        //asign a drawable left of attachment and custom like images and showing name of file here
                        contact_last_msg.setText("MEDIA CONTENT");
                    }else {

                        String last_msg = mediaAndMessages.getString("content");
                        if (last_msg.length() >= 32) {
                            last_msg = last_msg.substring(0, 32);
                            last_msg = last_msg.concat("...");
                        }
                        contact_last_msg.setText(last_msg);

                    }
                        //break the else here


                        if(cursor.getString(1).compareTo(thisfriends.get(position))==0)//if sender== friend
                        {
                            //the double tick image will be null
                            tick.setImageResource(R.drawable.doubleright24pxwhite);
                            //tick.setMaxHeight(24);
                            //tick.setMaxWidth(24);
                        }
                        else if(cursor.getString(2).compareTo(thisfriends.get(position))==0)
                        {
                            if(cursor.getString(4).compareTo("Not Send")==0)
                            {
                                //no tick
                                tick.setImageResource(R.drawable.clock24pxwhite);
                                //Toast.makeText(getActivity(), "NOT SEND", Toast.LENGTH_SHORT).show();
                            }
                            else if(cursor.getString(4).compareTo("Send")==0)
                            {
                                //tick
                                tick.setImageResource(R.drawable.checkmark24pxwhite);
                                // Toast.makeText(getActivity(), "SEND", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                tick.setImageResource(R.drawable.doubletick24pxwhite);
                            }
                        }

                } catch (JSONException e) {
                    e.printStackTrace();
                }






            }


            return convertView;
        }


    }

}