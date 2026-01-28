package com.example.wordpuzzlegame;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class AdminAddWords extends AppCompatActivity {

    EditText etWord;
    Spinner spDifficulty;
    Button btnAddWord;
    ListView listWords;

    ArrayList<String> wordsList;
    ArrayAdapter<String> listAdapter;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_words);

        dbHelper = new DBHelper(this);

        etWord = findViewById(R.id.etWord);
        spDifficulty = findViewById(R.id.spDifficulty);
        btnAddWord = findViewById(R.id.btnAddWord);
        listWords = findViewById(R.id.listWords);

        // Spinner adapter
        ArrayAdapter<CharSequence> spinnerAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.difficulty_levels,
                        android.R.layout.simple_spinner_item
                );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDifficulty.setAdapter(spinnerAdapter);


        wordsList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                wordsList
        );
        listWords.setAdapter(listAdapter);

        loadWords();

        btnAddWord.setOnClickListener(v -> addWord());

        spDifficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                loadWords();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void addWord() {
        String word = etWord.getText().toString().trim().toUpperCase();
        String difficulty = spDifficulty.getSelectedItem().toString();

        if (word.isEmpty()) {
            Toast.makeText(this, "Enter a word", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = dbHelper.addWord(word, difficulty);

        if (!inserted) {
            Toast.makeText(this, "Word already exists", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Word added successfully", Toast.LENGTH_SHORT).show();
            etWord.setText("");
            loadWords();
        }
    }

    private void loadWords() {
        wordsList.clear();
        String difficulty = spDifficulty.getSelectedItem().toString();

        Cursor c = dbHelper.getWords(difficulty);
        if (c.moveToFirst()) {
            do {
                wordsList.add(c.getString(c.getColumnIndexOrThrow("word")));
            } while (c.moveToNext());
        }
        c.close();

        listAdapter.notifyDataSetChanged();
    }
}
