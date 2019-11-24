package com.example.ghazanfer.messenger_fin;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;


import okhttp3.OkHttpClient;

public class okhttpSingleton {

    public static final String MYBASEURL="http://168.211.136.241/okhttp/";
    public static final String MYURL=MYBASEURL+"index.php";
    public static final String SPECIAL_ACCESS_KEY="dc3a2828b7ffb1ddaff3cdafb081df13";

    private static okhttpSingleton minstance;
    private OkHttpClient okHttpClient;
    private okhttpSingleton(){
    }
    public static okhttpSingleton gethttpSingleton(){
        if(minstance==null){
            minstance=new okhttpSingleton();
        }
        return minstance;
    }

    public OkHttpClient getOkHttpClient() {
        if(okHttpClient==null)okHttpClient=new OkHttpClient();
        return okHttpClient;
    }





}
