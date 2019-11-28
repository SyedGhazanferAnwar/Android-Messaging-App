package com.example.ghazanfer.messenger_fin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Ghazanfer on 10-Jan-18.
 */

/*
    1. Text message send
    2.Text message Receive
    3.Image message send
    4.Image message Receive
    5.media message send
    6.media message Receive
 */

public class recyclerAdapter extends RecyclerView.Adapter{

    LayoutInflater inflator;
    static ArrayList<singleMessage2> list;
    String username;
    Context context;
    recyclerAdapter(Context c,ArrayList<singleMessage2> myList,String Username)
    {
        context=c;
        list=myList;
        username=Username;
    }


    @Override
    public int getItemViewType(int position) {
        singleMessage2 obj=list.get(position);
        if(obj.Sender.toLowerCase().compareTo(username.toLowerCase())==0 && obj.mediaType==null){
            return 1;//sender is user
        }
        else if(obj.receiver.toLowerCase().compareTo(username.toLowerCase())==0 && obj.mediaType==null){
            return 2;
        }
        else if(obj.Sender.toLowerCase().compareTo(username.toLowerCase())==0){
            if(obj.mediaType.toLowerCase().equals(".jpg") || obj.mediaType.toLowerCase().equals(".png") || obj.mediaType.toLowerCase().equals(".jpeg")) return 3;
            //else return 5;//means currect user send some media
        }
        else if(obj.receiver.toLowerCase().compareTo(username.toLowerCase())==0){
            if(obj.mediaType.toLowerCase().equals(".jpg") || obj.mediaType.toLowerCase().equals(".png") ||obj.mediaType.toLowerCase().equals(".jpeg")) return 4;
            //else return 6;//means currect user received some media
        }
        Log.d("WHY", "getItemViewType: "+obj.Sender+"   "+username+"   "+obj.receiver+"  "+obj.mediaType);
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        inflator=LayoutInflater.from(context);
        if(viewType==1)//sender is user
        {
            view=inflator.inflate(R.layout.custom_row_message_sender,parent,false);
            return new sendMessageHolder(view);
        }
        else if(viewType==2)
        {
            view=inflator.inflate(R.layout.custom_row_message_receive,parent,false);
            return new receiveMessageHolder(view);
        }
        else if(viewType==3){
            view=inflator.inflate(R.layout.custom_row_image_sender,parent,false);
            return new sendImageHolder(view);
        }
        else if(viewType==4){
            view=inflator.inflate(R.layout.custom_row_image_receive,parent,false);
            return new receiveImageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        singleMessage2 obj=list.get(position);
        if(holder.getItemViewType()==1)
        {
            String message=obj.message;
            String status=obj.status;
            String date=obj.time;
            ((sendMessageHolder) holder).bind(message,status,date);
        }
        else if(holder.getItemViewType()==2)
        {

            String message=obj.message;
            String date=obj.time;
            ((receiveMessageHolder)holder).bind(message,date);
        }
        else if(holder.getItemViewType()==3)
        {
            String imagePath=obj.mediaPath;
            String status=obj.status;
            String date=obj.time;
            ((sendImageHolder)holder).bind(imagePath,status,date);
        }
        else if(holder.getItemViewType()==4)
        {
            String imagePath=obj.mediaPath;
            String date=obj.time;
            ((receiveImageHolder)holder).bind(imagePath,date);
        }

    }




    @Override
    public int getItemCount() {
        return list.size();
    }



    class receiveMessageHolder extends RecyclerView.ViewHolder
    {
        TextView text_message_body;
        TextView text_message_time;

        public receiveMessageHolder(View itemView) {
            super(itemView);
            text_message_body= (TextView) itemView.findViewById(R.id.text_message_body);
            text_message_time= (TextView) itemView.findViewById(R.id.text_message_time);
        }
        void bind(String userMessage,String time)
        {
            text_message_time.setText(time);
            text_message_body.setText(userMessage);
        }
    }


    class sendMessageHolder extends RecyclerView.ViewHolder
    {
        TextView text_message_body;
        TextView text_message_time;
        ImageView img_view;

        public sendMessageHolder(View itemView) {
            super(itemView);
            text_message_body= (TextView) itemView.findViewById(R.id.text_message_body);
            text_message_time= (TextView) itemView.findViewById(R.id.text_message_time);
            img_view= (ImageView) itemView.findViewById(R.id.statusImg);
        }

        void bind(String userMessage,String status,String date)
        {

            text_message_body.setText(userMessage);
            text_message_time.setText(date);
            if(status.compareTo("Not Send")==0)img_view.setImageResource(R.drawable.clock24pxwhite);
            else if(status.compareTo("Send")==0) img_view.setImageResource(R.drawable.checkmark24pxwhite);
            else img_view.setImageResource(R.drawable.doubletick24pxwhite);//sendReceived =status
            //if(status.compareTo("sendReceived")==0)
        }//sendReceived
    }

    class sendImageHolder extends RecyclerView.ViewHolder{

        ImageView imageviewsendermedia;
        TextView image_sender_message_time;
        ImageView statusImg_sender;
        public sendImageHolder(View itemView) {
            super(itemView);
            imageviewsendermedia=itemView.findViewById(R.id.imageviewsendermedia);
            image_sender_message_time=itemView.findViewById(R.id.image_sender_message_time);
            statusImg_sender=itemView.findViewById(R.id.statusImg_sender);
        }
        void bind(String imagePath,String status,String date)
        {
            Log.d("imagekapath", "bind: "+imagePath);
            Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);


            imageviewsendermedia.setImageBitmap(myBitmap);
            image_sender_message_time.setText(date);
            if(status.compareTo("Not Send")==0)statusImg_sender.setImageResource(R.drawable.clock24pxwhite);
            else if(status.compareTo("Send")==0) statusImg_sender.setImageResource(R.drawable.checkmark24pxwhite);
            else statusImg_sender.setImageResource(R.drawable.doubletick24pxwhite);//sendReceived =status
            //if(status.compareTo("sendReceived")==0)
        }//sendReceived
    }

    class receiveImageHolder extends RecyclerView.ViewHolder{

        ImageView imageviewreceivermedia;
        TextView image_receiver_message_time;
        ImageView statusImg_receiver;
        public receiveImageHolder(View itemView) {
            super(itemView);
            imageviewreceivermedia=itemView.findViewById(R.id.imageviewreceivermedia);
            image_receiver_message_time=itemView.findViewById(R.id.image_receiver_message_time);
        }
        void bind(String imagePath,String date)
        {
            Log.d("imagekapath", "bind: "+imagePath);
           // Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);


           // imageviewreceivermedia.setImageBitmap(myBitmap);
            image_receiver_message_time.setText(date);
        }//sendReceived
    }
}
