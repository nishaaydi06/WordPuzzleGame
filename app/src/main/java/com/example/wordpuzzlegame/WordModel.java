package com.example.wordpuzzlegame;



import java.util.ArrayList;
import java.util.List;

public class WordModel {
    public String word;
    public boolean found = false;
    public List<int[]> positions = new ArrayList<>();

    public WordModel(String word) {
        this.word = word;
    }
}

