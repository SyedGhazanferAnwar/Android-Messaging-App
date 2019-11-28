package com.example.ghazanfer.messenger_fin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity{

    Button signin;
    EditText username;
    EditText password;
    String sent;
    TextView register;
    Handler x;
    ScrollView scrollView;
    //Encryption encryption;
    SharedPreferences mySharedPreferences;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        scrollView= (ScrollView) findViewById(R.id.myscrollview);
        final View activityRootView = findViewById(R.id.myscrollview);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                //keyboard is visible
                if (heightDiff > 100) {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }
        });
        Log.d("poppp", "onCreate: ");
        int mode= Activity.MODE_PRIVATE;
        //encryption = Encryption.getDefault("a", "a", new byte[16]);
        mySharedPreferences=getSharedPreferences("MySharedPreference",mode);
        //Retreiving data to identify if loged in
        //_sto stands for store
            String logedInInfo_sto=mySharedPreferences.getString("LogedInInfo","null");
            String password_sto=mySharedPreferences.getString("password","null");
            final String username_sto=mySharedPreferences.getString("username","null");

            Log.d("TEST12", logedInInfo_sto+" _login"+username_sto+" _username");

                if("LogedIn".equals(logedInInfo_sto)){
                    Messaging_UI_FRAG.username=username_sto;
                    Backgroundmessaging_Service.username=username_sto;
                    Log.d("TEST200", logedInInfo_sto+" _login"+username_sto+" _username");


                    Intent intent=new Intent(MainActivity.this,frag_messaging_Activity.class);
                    startActivity(intent);
                    return;
                }



        //Retreiving data to identify if loged in DONE


        signin= (Button) findViewById(R.id.signin);
        username= (EditText) findViewById(R.id.username);
        password= (EditText) findViewById(R.id.password);
        register= (TextView) findViewById(R.id.createaccount);
        x=new Handler();
////        ObjectMapper mapper=new ObjectMapper();
////        String zo="";
////        try {
////            zo = mapper.writeValueAsString(x);
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//        //System.out.println(zo);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        Intent intent=new Intent(MainActivity.this,register_Activity.class);
                        startActivity(intent);
            }
        });
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username_val=username.getText().toString().toLowerCase();
                final String password_val=password.getText().toString();
                if(username_val.contains("[")||username_val.contains("]"))
                {
                    //Toast.makeText(MainActivity.this, "INVALID USERNAME", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(username_val.isEmpty()){
                    username.setError("Username is empty");
                    return;
                }
                if(password_val.isEmpty()){
                    password.setError("Password is empty");
                    return;
                }
                final ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Authenticating");
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                            OkHttpClient okHttpClient=okhttpSingleton.gethttpSingleton().getOkHttpClient();
                            JSONObject obj=new JSONObject();
                            obj.put("username",username_val);
                            obj.put("password",password_val);
                            RequestBody requestBody=new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("special_key",okhttpSingleton.SPECIAL_ACCESS_KEY)
                                    .addFormDataPart("operation","login")
                                    .addFormDataPart("credentials",obj.toString())
                                    .build();
                            Request request=new Request.Builder().url(okhttpSingleton.MYURL).post(requestBody).build();
                            Response response=okHttpClient.newCall(request).execute();
                            String responseString=response.body().string();
                            Log.d("mynewtag", responseString);
                            JSONObject readobj=new JSONObject(responseString);
                            if(readobj.get("result").equals("Success")){
                                Messaging_UI_FRAG.username=username_val;
                                Backgroundmessaging_Service.username=username_val;
                                JSONObject user=new JSONObject(readobj.getString("user"));
                                String login_unique_id=user.getString("login_unique_id");

                                String myfullname=user.getString("name");


                                //STORING DATA TO IDENTIFY if loged in
                                SharedPreferences.Editor editor= mySharedPreferences.edit();

//                                String logedInInfo=encryption.encrypt("LogedIn");
//                                String username=encryption.encrypt(username_val);
//                                String password=encryption.encrypt(password_val);
                                editor.putString("LogedInInfo","LogedIn");
                                editor.putString("password",password_val);
                                editor.putString("username",username_val);
                                editor.putString("fullname",myfullname);
                                editor.putString("login_unique_id",login_unique_id);
                                editor.commit();
                                Log.d("TEST2", " _login"+username+" _username");
                                x.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                });
                                frag_messaging_Activity.username=username_val;
                                Intent intent=new Intent(MainActivity.this,frag_messaging_Activity.class);
                                startActivity(intent);
                                return;
                            }
                            else
                            {
                                //Implement logic to show some message
                                x.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        AlertDialog.Builder builder;

                                            builder=new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle("Login Failed").setMessage("Password or Username is incorrect").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //do nothing
                                            }
                                        }).show();
                                    }
                                });
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            x.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    AlertDialog.Builder builder;
                                    builder=new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Connection Lost").setMessage("There was some issue with your connection.Please try Again!").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //do nothing
                                        }
                                    }).show();

                                }
                            });

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                    }
                }).start();

            }

        });

//        Intent intent=new Intent(this,register_Activity.class);
//        startActivity(intent);
    }
    public void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


}
