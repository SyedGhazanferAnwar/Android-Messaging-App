/**
 * Created by Ghazanfer on 30-Dec-17.
 */
package com.example.ghazanfer.messenger_fin;


public class singleMessage {


    public String message;
    public String Sender;
    public String date;
    public String receiver;
    public boolean receivedStatus;
    public boolean receivedStatusSender;
    public long msg_id=-1;

    public int n=0;
    public String[] friends;
    public String[] content;

    public singleMessage(String message, String Sender, String date,String receiver,int x) {
        this.message = message;
        this.Sender = Sender;
        this.date = date;
        this.receiver=receiver;

        n=x;
        friends=new String[n];
        content=new String[n];
    }
    public singleMessage(int x)
    {
        n=x;
        friends=new String[n];
        content=new String[n];
    }
    public singleMessage() {
    }

}

