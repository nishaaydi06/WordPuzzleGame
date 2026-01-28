package com.example.wordpuzzlegame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "WordGameDB";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Users table
        db.execSQL(
                "CREATE TABLE Users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT UNIQUE," +
                        "password TEXT," +
                        "isAdmin INTEGER DEFAULT 0)"
        );


        db.execSQL(
                "CREATE TABLE Words (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "word TEXT," +
                        "difficulty TEXT," +
                        "UNIQUE(word, difficulty))"
        );



        db.execSQL(
                "CREATE TABLE HighScores (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "userId INTEGER," +
                        "score INTEGER," +
                        "difficulty TEXT," +
                        "FOREIGN KEY(userId) REFERENCES Users(id))"
        );


        db.execSQL(
                "INSERT OR IGNORE INTO Users(username, password, isAdmin) " +
                        "VALUES('admin', 'admin123', 1)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS HighScores");
        db.execSQL("DROP TABLE IF EXISTS Words");
        db.execSQL("DROP TABLE IF EXISTS Users");
        onCreate(db);
    }



    public Cursor checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM Users WHERE username=? AND password=?",
                new String[]{username, password}
        );
    }

    public boolean isUserAdmin(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT isAdmin FROM Users WHERE id=?",
                new String[]{String.valueOf(userId)}
        );

        boolean isAdmin = false;
        if (c.moveToFirst()) {
            isAdmin = c.getInt(0) == 1;
        }
        c.close();
        return isAdmin;
    }


    public void addUser(String username, String password, int isAdmin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        cv.put("isAdmin", isAdmin);
        db.insertOrThrow("Users", null, cv);
    }




    public boolean addWord(String word,String difficulty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("word", word.toUpperCase());
        cv.put("difficulty", difficulty);

        long result = db.insertWithOnConflict(
                "Words",
                null,
                cv,
                SQLiteDatabase.CONFLICT_IGNORE
        );

        return result != -1;
    }


    public Cursor getWords(String difficulty) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT word FROM Words WHERE difficulty=?",
                new String[]{difficulty}
        );
    }


    public List<String> getWordsForDifficulty(String difficulty) {
        List<String> words = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT word FROM Words WHERE difficulty=?",
                new String[]{difficulty}
        );

        while (c.moveToNext()) {
            words.add(c.getString(0));
        }

        c.close();
        return words;
    }




    public void saveHighScore(int userId, int score, String difficulty) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery(
                "SELECT score FROM HighScores WHERE userId=? AND difficulty=?",
                new String[]{String.valueOf(userId), difficulty}
        );

        if (c.moveToFirst()) {
            int oldScore = c.getInt(0);
            if (score > oldScore) {
                ContentValues cv = new ContentValues();
                cv.put("score", score);
                db.update(
                        "HighScores",
                        cv,
                        "userId=? AND difficulty=?",
                        new String[]{String.valueOf(userId), difficulty}
                );
            }
        } else {
            ContentValues cv = new ContentValues();
            cv.put("userId", userId);
            cv.put("score", score);
            cv.put("difficulty", difficulty);
            db.insert("HighScores", null, cv);
        }

        c.close();
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, username FROM Users", null);
    }


    public void updateUserPassword(int userId, String newPass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", newPass);
        db.update("Users", cv, "id=?", new String[]{String.valueOf(userId)});
    }


    public void deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Users", "id=?", new String[]{String.valueOf(userId)});
    }

    public int getHighScore(int userId, String difficulty) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT score FROM HighScores WHERE userId=? AND difficulty=?",
                new String[]{String.valueOf(userId), difficulty}
        );

        int score = 0;
        if (c.moveToFirst()) {
            score = c.getInt(0);
        }

        c.close();
        return score;
    }
}
