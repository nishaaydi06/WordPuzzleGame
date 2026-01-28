package com.example.wordpuzzlegame;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.*;

public class GameActivity extends AppCompatActivity {

    GridLayout gridLayout;
    LinearLayout cluesLayout;
    TextView tvScore, tvDifficulty;

    DBHelper dbHelper;
    int userId;
    String difficulty;

    int GRID_SIZE;
    int cellSize;

    char[][] grid;
    TextView[][] cells;

    int score = 0;
    int pointsPerWord = 10;


    List<TextView> selectedCells = new ArrayList<>();
    StringBuilder selectedWord = new StringBuilder();
    int startRow = -1, startCol = -1;
    int dirRow = 0, dirCol = 0;


    List<WordModel> words = new ArrayList<>();
    Map<String, TextView> clueViews = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gridLayout = findViewById(R.id.gridLayout);
        cluesLayout = findViewById(R.id.cluesLayout);
        tvScore = findViewById(R.id.tvScore);
        tvDifficulty = findViewById(R.id.tvDifficulty);

        dbHelper = new DBHelper(this);


        userId = getIntent().getIntExtra("userId", 0);
        difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) difficulty = "Easy";

        tvDifficulty.setText("Difficulty: " + difficulty);
        tvScore.setText("Score: 0");

        switch (difficulty) {
            case "Easy": GRID_SIZE = 8; pointsPerWord = 10; break;
            case "Medium": GRID_SIZE = 10; pointsPerWord = 20; break;
            case "Hard": GRID_SIZE = 12; pointsPerWord = 30; break;
            default: GRID_SIZE = 8;
        }

        gridLayout.setRowCount(GRID_SIZE);
        gridLayout.setColumnCount(GRID_SIZE);

        grid = new char[GRID_SIZE][GRID_SIZE];
        cells = new TextView[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) Arrays.fill(grid[i], '.');

        createGrid();
        loadWords();
        fillGridUI();
    }


    private void createGrid() {

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int gridSizePx = (int) (300 * dm.density); // fixed grid size
        cellSize = gridSizePx / GRID_SIZE;

        gridLayout.removeAllViews();
        gridLayout.setPadding(8, 8, 8, 8);

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {

                TextView tv = new TextView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.rowSpec = GridLayout.spec(r);
                params.columnSpec = GridLayout.spec(c);

                tv.setLayoutParams(params);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(Color.BLACK);
                tv.setTextSize(GRID_SIZE >= 12 ? 12 : 14);
                tv.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                tv.setTag(r + "," + c);

                cells[r][c] = tv;
                gridLayout.addView(tv);
            }
        }

        gridLayout.setOnTouchListener(gridTouchListener);
    }


    private void loadWords() {
        List<String> dbWords = dbHelper.getWordsForDifficulty(difficulty);
        if (dbWords == null) dbWords = new ArrayList<>();

        for (String w : dbWords) {
            WordModel wm = new WordModel(w.toUpperCase());
            words.add(wm);
            placeWord(wm.word, wm);
            addClue(wm.word);
        }
    }

    private void placeWord(String word, WordModel wm) {
        int[][] directions = {{0,1},{1,0},{1,1},{-1,1}};
        Random rand = new Random();

        for (int attempt = 0; attempt < 100; attempt++) {
            int[] d = directions[rand.nextInt(directions.length)];
            int r = rand.nextInt(GRID_SIZE);
            int c = rand.nextInt(GRID_SIZE);

            if (canPlace(word, r, c, d[0], d[1])) {
                wm.positions.clear();
                for (int i = 0; i < word.length(); i++) {
                    int nr = r + i * d[0];
                    int nc = c + i * d[1];
                    grid[nr][nc] = word.charAt(i);
                    wm.positions.add(new int[]{nr, nc});
                }
                return;
            }
        }
    }

    private boolean canPlace(String word, int r, int c, int dr, int dc) {
        for (int i = 0; i < word.length(); i++) {
            int nr = r + i * dr;
            int nc = c + i * dc;
            if (nr < 0 || nc < 0 || nr >= GRID_SIZE || nc >= GRID_SIZE) return false;
            if (grid[nr][nc] != '.' && grid[nr][nc] != word.charAt(i)) return false;
        }
        return true;
    }

    private void fillGridUI() {
        Random rand = new Random();
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (grid[r][c] == '.') grid[r][c] = (char) ('A' + rand.nextInt(26));
                cells[r][c].setText(String.valueOf(grid[r][c]));
            }
        }
    }

    private void addClue(String word) {
        TextView tv = new TextView(this);
        tv.setText(word);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(16);
        cluesLayout.addView(tv);
        clueViews.put(word, tv);
    }

    private final View.OnTouchListener gridTouchListener = (v, event) -> {

        float x = event.getX() - v.getPaddingLeft();
        float y = event.getY() - v.getPaddingTop();

        int r = (int) (y / cellSize);
        int c = (int) (x / cellSize);

        if (r < 0 || r >= GRID_SIZE || c < 0 || c >= GRID_SIZE) return true;

        TextView cell = cells[r][c];

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clearSelection();
                startRow = r;
                startCol = c;
                addCell(cell);
                break;

            case MotionEvent.ACTION_MOVE:
                if (selectedCells.size() == 1) {
                    dirRow = Integer.compare(r - startRow, 0);
                    dirCol = Integer.compare(c - startCol, 0);
                }
                if (isValidNextCell(r, c)) addCell(cell);
                break;

            case MotionEvent.ACTION_UP:
                checkWord();
                break;
        }
        return true;
    };

    private boolean isValidNextCell(int r, int c) {
        if (selectedCells.isEmpty()) return true;
        TextView last = selectedCells.get(selectedCells.size() - 1);
        String[] p = last.getTag().toString().split(",");
        int lr = Integer.parseInt(p[0]);
        int lc = Integer.parseInt(p[1]);
        return r == lr + dirRow && c == lc + dirCol;
    }

    private void addCell(TextView cell) {
        if (selectedCells.contains(cell)) return;
        selectedCells.add(cell);
        selectedWord.append(cell.getText());
        cell.setBackgroundColor(Color.YELLOW);
    }

    private void clearSelection() {
        for (TextView tv : selectedCells) {
            String[] p = tv.getTag().toString().split(",");
            int r = Integer.parseInt(p[0]);
            int c = Integer.parseInt(p[1]);
            if (!isCellFound(r, c))
                tv.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        }
        selectedCells.clear();
        selectedWord.setLength(0);
        dirRow = dirCol = 0;
    }

    private boolean isCellFound(int r, int c) {
        for (WordModel w : words) {
            if (w.found) {
                for (int[] p : w.positions)
                    if (p[0] == r && p[1] == c) return true;
            }
        }
        return false;
    }

    private void checkWord() {
        String attempt = selectedWord.toString();
        String reversed = new StringBuilder(attempt).reverse().toString();

        for (WordModel wm : words) {
            if (!wm.found && (wm.word.equals(attempt) || wm.word.equals(reversed))) {

                wm.found = true;
                score += pointsPerWord;
                tvScore.setText("Score: " + score);

                for (int[] p : wm.positions)
                    cells[p[0]][p[1]].setBackgroundColor(Color.GREEN);

                TextView clue = clueViews.get(wm.word);
                if (clue != null) {
                    clue.setPaintFlags(clue.getPaintFlags() |
                            android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                    clue.setTextColor(Color.GRAY);
                }

                clearSelection();

                if (allWordsFound()) {
                    dbHelper.saveHighScore(userId, score, difficulty);
                    showFinishDialog();
                }
                return;
            }
        }
        clearSelection();
    }

    private boolean allWordsFound() {
        for (WordModel w : words) if (!w.found) return false;
        return true;
    }

    private void showFinishDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🏆 Game Completed!")
                .setMessage("Final Score: " + score)
                .setPositiveButton("OK", (d, w) -> finish())
                .show();
    }
}
