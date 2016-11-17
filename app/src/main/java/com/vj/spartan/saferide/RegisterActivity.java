package com.vj.spartan.saferide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    RequestQueue queue;
    EditText firstName;
    EditText lastName;
    EditText studentId;
    EditText sjsuEmail;
    EditText password;
    EditText confirmPassword;
    String message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        queue = Volley.newRequestQueue(this);
        firstName = (EditText)findViewById(R.id.input_first_name);
        lastName = (EditText)findViewById(R.id.input_last_name);
        studentId = (EditText)findViewById(R.id.input_student_id);
        sjsuEmail = (EditText)findViewById(R.id.input_email);
        password = (EditText)findViewById(R.id.input_password);
        confirmPassword = (EditText) findViewById(R.id.input_confirm_password);



        Button register = (Button) findViewById(R.id.btn_signup);
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (password.getText().toString().equals(confirmPassword.getText().toString())) {
                    try {
                        postSignUpDetails();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Password not equal", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void close(View view)    {
        finish();
    }

    public void postSignUpDetails() throws JSONException {

        Map<String, String> params = new HashMap<String, String>();

        params.put("first_name", firstName.getText().toString());
        params.put("last_name", lastName.getText().toString());
        params.put("sjsu_id", studentId.getText().toString());
        params.put("email", sjsuEmail.getText().toString());
        params.put("password", password.getText().toString());

        String url = "http://saferide.nagkumar.com/client_sign_up/";
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response", "Response received: " + response);
                try {
                    message = response.getString("message");
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
