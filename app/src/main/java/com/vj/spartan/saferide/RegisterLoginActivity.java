package com.vj.spartan.saferide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RegisterLoginActivity extends AppCompatActivity {

    Button register;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_login);
        register = (Button)findViewById(R.id.btn_register_screen);
        login = (Button) findViewById(R.id.btn_login_screen);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentRegister = new Intent(RegisterLoginActivity.this, RegisterActivity.class);
                RegisterLoginActivity.this.startActivity(intentRegister);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentLogin = new Intent(RegisterLoginActivity.this, LoginActivity.class);
                RegisterLoginActivity.this.startActivity(intentLogin);
            }
        });
    }
}
