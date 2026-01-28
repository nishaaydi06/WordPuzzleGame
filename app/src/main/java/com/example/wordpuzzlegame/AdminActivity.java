package com.example.wordpuzzlegame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    Button btnAddWords, btnManageUsers;
    DBHelper dbHelper;
    int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // Your new menu layout

        dbHelper = new DBHelper(this);
        currentUserId = getIntent().getIntExtra("userId", -1);


        if(currentUserId == -1 || !dbHelper.isUserAdmin(currentUserId)){
            Toast.makeText(this,"Access Denied",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnAddWords = findViewById(R.id.btnAddWords);
        btnManageUsers = findViewById(R.id.btnManageUsers);

        btnAddWords.setOnClickListener(v -> {
            Intent i = new Intent(AdminActivity.this, AdminAddWords.class);
            startActivity(i);
        });


        btnManageUsers.setOnClickListener(v -> {
            Intent i = new Intent(AdminActivity.this, AdminUsersActivity.class);
            startActivity(i);
        });
    }
}
