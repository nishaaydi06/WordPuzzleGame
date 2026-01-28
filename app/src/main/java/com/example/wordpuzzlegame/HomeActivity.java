package com.example.wordpuzzlegame;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class HomeActivity extends AppCompatActivity {

    Spinner spinnerDifficulty;
    Button btnStart;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userId = getIntent().getIntExtra("userId", 0);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        btnStart = findViewById(R.id.btnStart);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.difficulty_levels,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);


        btnStart.setOnClickListener(v -> {
            // Prevent null crash
            if (spinnerDifficulty.getSelectedItem() == null) return;

            String difficulty = spinnerDifficulty.getSelectedItem().toString();
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("difficulty", difficulty);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }
}
