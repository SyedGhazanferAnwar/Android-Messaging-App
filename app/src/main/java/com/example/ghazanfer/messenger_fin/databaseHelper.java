package com.example.ghazanfer.messenger_fin;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Ghazanfer on 05-Jan-18.
 */

//MAde singleton to avoid leakage
public class databaseHelper extends SQLiteOpenHelper {
    private static SharedPreferences mySharedPreferences;
    Context con;
    private static databaseHelper mdatabaseHelper=null;
    static String username;
    private databaseHelper(Context context)
    {
        super(context,"messages"+username,null,1);
        con=context;
    }
    public static databaseHelper getDatabaseHelper(Context context){
        int mode= Activity.MODE_PRIVATE;
        mySharedPreferences=context.getSharedPreferences("MySharedPreference",mode);
        username=mySharedPreferences.getString("username",null);
        if(mdatabaseHelper==null)mdatabaseHelper=new databaseHelper(context);
        return mdatabaseHelper;
    }
    public static void closeDatabaseHelper(){
        mdatabaseHelper.close();
        mdatabaseHelper=null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Call here a method to get all data when loged in first time
        username=mySharedPreferences.getString("username",null);
        Log.d("database", "onCreate: Dataase created messages"+username);

        String createTable="CREATE TABLE messages"+username+" (ID INTEGER PRIMARY KEY AUTOINCREMENT, Sender TEXT,Receiver TEXT,Content TEXT,messageStatus TEXT,Time TEXT);";
        //Toast.makeText(con, "DATABASE IS CRTEATED", Toast.LENGTH_SHORT).show();
        try{
            db.execSQL(createTable);

        }catch (SQLException e)
        {
            Toast.makeText(con, e+"", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try{
            username=mySharedPreferences.getString("username",null);
            db.execSQL("DROP IF TABLE EXISTS messages"+username);
        }catch (SQLException e)
        {
            Toast.makeText(con, e+"", Toast.LENGTH_SHORT).show();
        }
        onCreate(db);
    }

    public long addData(String sender,String receiver,String content,String messageStaus,String Time)
    {
        username=mySharedPreferences.getString("username",null);

        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("Sender",sender);
        contentValues.put("Receiver",receiver);
        contentValues.put("Content",content);
        contentValues.put("messageStatus",messageStaus);
        contentValues.put("Time",Time);
        long inserted=db.insert("messages"+username,null,contentValues);
        Log.d("CHECKER!2", "run: "+username);

        return inserted;
    }

    public Cursor getData(String query)
    {
        try {
            SQLiteDatabase db=this.getWritableDatabase();
            Cursor data=db.rawQuery(query,null);
            return data;
        }catch (SQLException e){
            Log.d("pakar liaa", "getData: "+e);
        }
        return null;
    }
    public boolean updateSend(long id,String messageStatus)
    {
        username=mySharedPreferences.getString("username",null);

        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("messageStatus",messageStatus);
        db.update("messages"+username,contentValues,"ID="+id,null);
        return true;
    }

    public boolean updateReceived(long id,String messageStatus)
    {
        username=mySharedPreferences.getString("username",null);

        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("messageStatus",messageStatus);
        db.update("messages"+username,contentValues,"ID="+id,null);
        return true;
    }

}
