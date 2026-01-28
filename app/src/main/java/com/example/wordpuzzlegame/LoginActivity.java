package com.example.wordpuzzlegame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    TextView tvRegisterLink;
    Button btnLogin;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        dbHelper = new DBHelper(this);


        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });


        btnLogin.setOnClickListener(v -> {

            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor c = dbHelper.checkLogin(user, pass);

            if (c.moveToFirst()) {

                int userId = c.getInt(c.getColumnIndexOrThrow("id"));
                int isAdmin = c.getInt(c.getColumnIndexOrThrow("isAdmin"));
                c.close();

                if (isAdmin == 1) {
                    // 👑 ADMIN
                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                } else {

                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                }

                finish();

            } else {
                c.close();
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
