package com.example.ghazanfer.messenger_fin;

import java.io.File;

/**
 * Created by Ghazanfer on 04-Jan-18.
 */

public interface Communicator {
    public void addFriendServerReq(String searchText,int offset);
    public void updateMessageUi(String update,long id);
    public void sendMessage(String sender,String receiver,String content,String time,long id);
//    public void userListPopulator();
    public void messageOpener(String username);
    public void updateList(String LastMessage,String LastMessageContact);
    public void setMessageStatus(int status);
    public void resetConnection();
    public void contactRequest(String[] contactsUsername,String[] fullname,String[] status);
    public void setName(String name);
    public void sendFriendRequest(String requestedUsername);
    public void cancelSendFriendRequest(String requestedUsername);
    public void acceptFriendRequest(String requestedUsername);
    public void getFriends(int override);
    public void contactListNotifyDataChange();
    public void setLastSeenArray(String x);//For contacts Frag

    public void sendFile(File x,String mime_type,String receiver);
    public void fileSentNotify();
}
