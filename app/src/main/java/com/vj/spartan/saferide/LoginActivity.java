package com.vj.spartan.saferide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity {
    RequestQueue queue;
    EditText sjsuEmail;
    EditText password;
    Button login;
    String message;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        queue = Volley.newRequestQueue(this);
        sjsuEmail = (EditText)findViewById(R.id.input_login_sjsuEmail);
        password = (EditText) findViewById(R.id.input_login_password);
        login = (Button)findViewById(R.id.btn_login);

        login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    postLoginDetails();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void setToken(){

        sharedpreferences = getSharedPreferences("authToken", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("token", message);
        editor.commit();
        String text = sharedpreferences.getString("token", "No token stored");
        Log.i("TAG", text);
        Intent myIntent = new Intent(LoginActivity.this, MapsActivity.class);
        //myIntent.putExtra("key", value); //Optional parameters
        LoginActivity.this.startActivity(myIntent);

    }

    public void postLoginDetails() throws JSONException {

        Map<String, String> params = new HashMap<String, String>();

        params.put("username", sjsuEmail.getText().toString());
        params.put("password", password.getText().toString());

        String url = "http://saferide.nagkumar.com/login/";
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response", "Response received: " + response);
                try {
                    message = response.getString("token");
                    Log.i("TAG", message);
                    setToken();
                    Toast.makeText(getApplicationContext(), message , Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", "error: " + error.toString());
                VolleyError verror = new VolleyError(new String(error.networkResponse.data));
                Log.d("Error", verror.toString());

            }
        });
        queue.add(postRequest);
    }
}

