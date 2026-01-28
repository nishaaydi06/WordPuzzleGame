package com.example.wordpuzzlegame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    EditText etNewUsername, etNewPassword;
    Button btnAddUser;
    ListView listUsers;
    DBHelper dbHelper;
    List<String> userList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        etNewUsername = findViewById(R.id.etNewUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnAddUser = findViewById(R.id.btnAddUser);
        listUsers = findViewById(R.id.listUsers);
        dbHelper = new DBHelper(this);

        userList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        listUsers.setAdapter(adapter);

        loadUsers();


        btnAddUser.setOnClickListener(v -> {
            String username = etNewUsername.getText().toString();
            String password = etNewPassword.getText().toString();
            if(username.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Enter username and password",Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                dbHelper.addUser(username, password, 0);
                Toast.makeText(this,"User added",Toast.LENGTH_SHORT).show();
                etNewUsername.setText(""); etNewPassword.setText("");
                loadUsers();
            } catch(Exception e){
                Toast.makeText(this,"User already exists",Toast.LENGTH_SHORT).show();
            }
        });

        listUsers.setOnItemClickListener((parent, view, position, id) -> {
            String selected = userList.get(position);
            String[] parts = selected.split(":"); // "1: username"
            int userId = Integer.parseInt(parts[0].trim());
            showEditDeleteDialog(userId);
        });
    }

    private void loadUsers(){
        userList.clear();
        Cursor c = dbHelper.getAllUsers();
        while(c.moveToNext()){
            int id = c.getInt(c.getColumnIndexOrThrow("id"));
            String username = c.getString(c.getColumnIndexOrThrow("username"));

            userList.add(id + ": " + username);
        }
        c.close();
        adapter.notifyDataSetChanged();
    }

    private void showEditDeleteDialog(int userId){


        if(dbHelper.isUserAdmin(userId)){
            Toast.makeText(this,"Admin user cannot be modified",Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit / Delete User");

        EditText input = new EditText(this);
        input.setHint("New Password (leave blank to keep)");
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newPass = input.getText().toString();
            if(!newPass.isEmpty()){
                dbHelper.updateUserPassword(userId, newPass);
                Toast.makeText(this,"Password updated",Toast.LENGTH_SHORT).show();
                loadUsers();
            }
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            dbHelper.deleteUser(userId);
            Toast.makeText(this,"User deleted",Toast.LENGTH_SHORT).show();
            loadUsers();
        });

        builder.show();
    }



    }

