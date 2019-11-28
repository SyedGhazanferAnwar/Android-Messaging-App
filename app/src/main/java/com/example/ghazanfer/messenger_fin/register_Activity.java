package com.example.ghazanfer.messenger_fin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
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
import java.net.Socket;
import java.net.UnknownHostException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class register_Activity extends AppCompatActivity {

    Button register;
    TextView signin;
    String sent;
    EditText username;
    EditText password;
    EditText email;
    EditText fullname;
    Handler x;
    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_);
        signin= (TextView) findViewById(R.id.signin_txt);
        register= (Button) findViewById(R.id.register_btn);
        username= (EditText) findViewById(R.id.username_reg);
        password= (EditText) findViewById(R.id.password_reg);
        fullname= (EditText) findViewById(R.id.fullname_reg);
        email = (EditText) findViewById(R.id.email_reg);
        scrollView= (ScrollView) findViewById(R.id.myscrollview);
        x=new Handler();
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
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        Intent intent=new Intent(register_Activity.this,MainActivity.class);
                        startActivity(intent);
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String username_val=username.getText().toString().toLowerCase();
                final String password_val=password.getText().toString();
                final String fullname_val=fullname.getText().toString();
                final String email_val=email.getText().toString();

                if(username_val.contains("{")||password_val.contains("}")||fullname_val.contains("[")||email_val.contains("]"))
                {
                    //******Do some checking to avoid html hacking On database this is not sufficient****//
                    Toast.makeText(register_Activity.this, "INVALID Crendentials", Toast.LENGTH_SHORT).show();
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
                if(fullname_val.isEmpty()){
                    fullname.setError("Name is empty");
                    return;
                }
                if(email_val.isEmpty()){
                    email.setError("Email is empty");
                    return;
                }
                final ProgressDialog progressDialog=new ProgressDialog(register_Activity.this);
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
                            obj.put("fullname",fullname_val);
                            obj.put("email",email_val);
                            RequestBody requestBody=new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("special_key",okhttpSingleton.SPECIAL_ACCESS_KEY)
                                    .addFormDataPart("operation","register")
                                    .addFormDataPart("credentials",obj.toString())
                                    .build();
                            Request request=new Request.Builder().url(okhttpSingleton.MYURL).post(requestBody).build();
                            Response response=okHttpClient.newCall(request).execute();
                            String responseString=response.body().string();
                            Log.d("mynewtag", responseString);
                            JSONObject readobj=new JSONObject(responseString);
                            if(readobj.get("result").equals("Success")){
                                x.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        AlertDialog.Builder builder;
                                        builder=new AlertDialog.Builder(register_Activity.this);
                                        builder.setTitle("Registration Successfull").setMessage("You have been Registered Successfully.Please Login to Continue!")
                                                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent=new Intent(register_Activity.this,MainActivity.class);
                                                startActivity(intent);

                                            }
                                        }).show();
                                    }
                                });

                            }
                            else {
                                //Toast.makeText(MainActivity.this, "LOGIN FAILED", Toast.LENGTH_SHORT).show();
                                x.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        AlertDialog.Builder builder;
                                        builder=new AlertDialog.Builder(register_Activity.this);
                                        builder.setTitle("Registration Failed").setMessage("Username is already taken.Please choose a different username!")
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                }).show();
                                    }
                                });
                            }

                            } catch (JSONException e1) {
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            x.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    AlertDialog.Builder builder;
                                    builder=new AlertDialog.Builder(register_Activity.this);
                                    builder.setTitle("Connection Lost").setMessage("There was some issue with your connection.Please try Again!").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //do nothing
                                        }
                                    }).show();

                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });


    }
}
