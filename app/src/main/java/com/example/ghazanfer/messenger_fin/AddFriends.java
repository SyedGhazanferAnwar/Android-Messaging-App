package com.example.ghazanfer.messenger_fin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;



//My Purple colour is #9b59b6

public class AddFriends extends Fragment {
    EditText searchbar;
    ImageButton right_cross_btn;
    Communicator comm;
    mycontactrequestAdapter adapter;
    Handler handle;
    String[] contacts_username;
    String[] fullname;
    String[] status;
    String[] empty;
    Fragment friendProfile;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    AddFriends addFriends;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    ListView search_list;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        comm= (Communicator) getActivity();
        View view=inflater.inflate(R.layout.fragment_add_friends, container, false);;
        searchbar= (EditText) view.findViewById(R.id.searchbar);
        search_list= (ListView) view.findViewById(R.id.search_list);

        //search_list.setAdapter(adapter);
        handle=new Handler();
        contacts_username=new String[0];
        empty=new String[0];
        adapter =new mycontactrequestAdapter(contacts_username);
        search_list.setAdapter(adapter);
        friendProfile=new FriendProfile();
        fragmentManager=getActivity().getSupportFragmentManager();
        addFriends=new AddFriends();
        right_cross_btn= (ImageButton) view.findViewById(R.id.right_cross_btn);
        right_cross_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchbar.setText("");
            }
        });
        searchbar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //PERFORM SEARCH
                    Log.d("POII", "onEditorAction: "+"!@##$% Get Friends:"+searchbar.getText());
                    //comm.sendMessage("!@##$% Get Friends:"+searchbar.getText(),-1);
                    comm.addFriendServerReq(searchbar.getText().toString(),0);
                    return true;
                }
                return false;
            }

        });


        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()!=0){
                    //comm.sendMessage("!@##$% Get Friends:"+s,-1);
                    comm.addFriendServerReq(searchbar.getText().toString(),0);
                    right_cross_btn.setImageResource(R.drawable.cross_white);
                }
                else {
                    contactRequestHandle(empty,empty,empty);
                    right_cross_btn.setImageResource(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        search_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               // TextView edname= (TextView) view.findViewById(R.id.textio);
                //String friendusername=edname.getText().toString();
                String friendusername="";

                Log.d("nametest", "onClick: "+friendusername);
                //comm.sendMessage("!@##$% Get User Info:"+friendusername,-1);

                if(!friendProfile.isAdded()) {
                    FriendProfile.profile_name=friendusername;
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.addfrind_relativelayout, friendProfile, "FriendProfile");
                    fragmentTransaction.remove(addFriends);
                    fragmentTransaction.addToBackStack("addFriends");
                    fragmentTransaction.commit();
                }

            }
        });




        return view;
    }
    public void setName(String name){
        FriendProfile.name=name;
    }

    public void contactRequestHandle(String[] contacts_param,String[] fullname_param,String[] status_param){//_param for parameter
        contacts_username=contacts_param;
        fullname=fullname_param;
        status=status_param;
        handle.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
//        Log.d("TAGII", "contactRequestHandle: "+contacts[0]);
        //adapter.notifyDataSetChanged();

    }



    class mycontactrequestAdapter extends BaseAdapter{

        public mycontactrequestAdapter(String[] contacts_param) {
            contacts_username=contacts_param;
        }

        @Override
        public int getCount() {
            return contacts_username.length;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView= getActivity().getLayoutInflater().inflate(R.layout.contactrequestlayout,null);

            TextView textViewusername= (TextView) convertView.findViewById(R.id.usernametextview);
            TextView textViewfullname= (TextView) convertView.findViewById(R.id.fullnametextview);
            textViewfullname.setText(fullname[position]);
            textViewusername.setText("@"+contacts_username[position]);
            final Button addbtn;
            addbtn= (Button) convertView.findViewById(R.id.addfriendbtn);

            if(status[position].equals("requestSend")){
                addbtn.setText("Pending...");
                addbtn.setBackgroundResource(R.drawable.button_rounded_rectangle_disabled);
            }else if(status[position].equals("requestReceived")){
                addbtn.setText("Accept");
                addbtn.setBackgroundResource(R.drawable.button_rounded_rectangle);
            }else if(status[position].equals("Friends")){
                addbtn.setText("Friends");
                addbtn.setBackgroundResource(R.drawable.button_rounded_rectangle_disabled);
            }
            addbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("debugcheckadd", status[position]);
                    if(addbtn.getText().equals("Pending...")){
                        addbtn.setText("Add");
                        addbtn.setBackgroundResource(R.drawable.button_rounded_rectangle);
                        comm.cancelSendFriendRequest(contacts_username[position]);
                    }else if(addbtn.getText().equals("Accept")){
                        addbtn.setText("Friends");
                        addbtn.setBackgroundResource(R.drawable.button_rounded_rectangle);
                        comm.acceptFriendRequest(contacts_username[position]);
                        comm.getFriends(1);
                        //call a function to accept the friend request
                    }else if(addbtn.getText().equals("Friends")){

                        //Show dialog do you want to unfriend if yes then unfrined
                        //call a funcion to unfriend
                    }else if(addbtn.getText().equals("Add")){
                        addbtn.setText("Pending...");
                        addbtn.setBackgroundResource(R.drawable.button_rounded_rectangle_disabled);
                        comm.sendFriendRequest(contacts_username[position]);
                    }
//                    addbtn.setText("Pending...");
//                    addbtn.setBackgroundResource(R.drawable.button_rounded_rectangle_disabled);
                    Log.d("add friends", "onClick: add freidn wala " +position);
//                    comm.sendFriendRequest(contacts_username[position]);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                comm.addFriendServerReq(searchbar.getText().toString(),0);

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            });
            return convertView;
        }
    }

}
