package fall2018.csc2017.slidingtiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import static fall2018.csc2017.slidingtiles.UtilityManager.TEMP_SAVE_FILENAME;
import static fall2018.csc2017.slidingtiles.UtilityManager.makeCustomToastText;
import static fall2018.csc2017.slidingtiles.UtilityManager.saveBoardsToAccounts;
import static fall2018.csc2017.slidingtiles.UtilityManager.saveScoresToAccounts;

/**
 * The game activity.
 */
public class GameActivity extends AppCompatActivity implements Observer {
    /**
     * The board manager.
     */
    private BoardManager boardManager;
    /**
     * The buttons to display.
     */
    private ArrayList<Button> tileButtons;
    /**
     * Interval in seconds for non-guest to auto-save;
     */
    public static final int SAVE_INTERVAL = 10000;

    /**
     * Grid View for the game
     */
    private GestureDetectGridView gridView;
    /**
     * width and height of the sliding tiles columns
     */
    private static int columnWidth, columnHeight;
    /**
     * the current account of the game player, null if guest player
     */
    private Account currentAccount;
    /**
     * the account's list of boards
     */
    private ArrayList<BoardManager> boardList;
    /**
     * the index of the current board being played on from the current account's list of boards
     */
    private int boardIndex;
    /**
     * the game timer
     */
    private Timer timer = new Timer();
    /**
     * timer task for this game activity
     */
    private TimerTask timerTask;
    /**
     * undo button for the game
     */
    private Button undoButton;
    /**
     * current game score
     */
    private Integer currentScore;
    /**
     * number of rows and columns on this game board
     */
    private int numRows, numColumns;
    private final Context ctx = this; //TODO: can we remove?
    /**
     * tile builder for the game board
     */
    private TileBuilder tileBuilder;
    /**
     * chronometer for the game
     */
    private Chronometer chronometer;
    /**
     * check if chronometer is running, true if yes
     */
    private boolean isChmRunning;
    /**
     * time when the game is pasued
     */
    private long pauseTime;
    /**
     * scoring system for the game
     */
    private ScoringSystem scoringSystem = new ScoringSystem();
    /**
     * Broken up images for background of tiles
     */
    public static ArrayList<Bitmap> IMAGE_SET;
    /**
     * Set up the background image for each button based on the master list
     * of positions, and then call the adapter to set the view.
     */
    // Display
    public void display() {
        updateTileButtons();
        gridView.setAdapter(new CustomAdapter(tileButtons, columnWidth, columnHeight));
        checkGameIsOver();
        undoButton = findViewById(R.id.UndoButton);
        undoButton.setText(getString(R.string.ga_undo, boardManager.getNumCanUndo()));
    }

    /**
     * A check if the board is solved and change to the scoreboard
     */
    private void checkGameIsOver() {
        if (boardManager.puzzleSolved()) {
            if(currentAccount!=null) {
                timer.cancel();
                timerTask.cancel();
            }
            pauseChronometer(chronometer);
            int movesTaken = boardManager.getMoves();
            boardManager.setTimeSpent(SystemClock.elapsedRealtime() - chronometer.getBase());
            int timeTaken = (int)boardManager.getTimeSpent() / 1000;
            currentScore = scoringSystem.calculateScore(movesTaken, timeTaken);
            gridView = findViewById(R.id.grid);
            Intent tmp = new Intent(gridView.getContext(), ScoreBoard.class);
            if(currentAccount != null) {
                currentAccount.addToSlidingGameScores(currentScore);
                saveScoresToAccounts(this, currentAccount, currentScore);
                tmp.putExtra("currentUsername", currentAccount.getUsername());
                tmp.putExtra("board", boardManager.getBoard());
                boardList.remove(boardManager);
                saveBoardsToAccounts(this,currentAccount,boardList);
            }
            else {
                tmp.putExtra("currentUsername", "-1");
            }
            tmp.putExtra("currentGame", "slidingTiles");
            tmp.putExtra("currentScore", currentScore);
            startActivity(tmp);
            IMAGE_SET = null;
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            loadFromFile(TEMP_SAVE_FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boardList = (ArrayList<BoardManager>) getIntent().getSerializableExtra("boardList");
        boardIndex = this.getIntent().getIntExtra("boardIndex", -1);
        setContentView(R.layout.activity_main);
        if(!GameSelection.IS_GUEST)
            setCurrentAccount((Account) getIntent().getSerializableExtra("account"));
        else
            setCurrentAccount(null);
        boolean useImage = IMAGE_SET != null;
        if(boardManager == null)
        {
            boardManager = boardList.get(boardIndex);
            numRows = boardManager != null? boardManager.getBoard().getNumRows(): 1;
            numColumns = boardManager != null? boardManager.getBoard().getNumColumns(): 1;
        }
        boardManager.setUseImage(useImage);
        if(boardManager.isUseImage()){
            boardManager.setCustomImageSet(IMAGE_SET);
        }
        addUndoButtonListener();
        chronometer = findViewById(R.id.chronometer);
        startChronometer(chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime() - boardManager.getTimeSpent());

        // Add View to activity
        gridView = findViewById(R.id.grid);
        gridView.setNumColumns(numColumns);
        gridView.setBoardManager(boardManager);
        boardManager.getBoard().addObserver(this);
        // Observer sets up desired dimensions as well as calls our display function
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        gridView.getViewTreeObserver().removeOnGlobalLayoutListener(
                                this);
                        int displayWidth = gridView.getMeasuredWidth();
                        int displayHeight = gridView.getMeasuredHeight();
                        columnWidth = displayWidth / numColumns;
                        columnHeight = displayHeight / numRows;
                        tileBuilder = new TileBuilder(boardManager, getBaseContext(), columnWidth);
                        tileBuilder.setUseImages(boardManager.isUseImage(), IMAGE_SET);
                        tileBuilder.createTileButtons();
                        tileButtons = tileBuilder.getTileButtons();

                        display();
                    }
                });
    }

    /**
     * Update the backgrounds on the buttons to match the tiles.
     */
    private void updateTileButtons() {
        Board board = boardManager.getBoard();
        int count = 0;
        for(int row = 0; row != numRows; row++){
            for(int col = 0; col != numColumns; col++){
                tileButtons.get(count).setBackground(board.getTile(row,col).getBackground());
                count++;
            }
        }
    }
    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        saveToFile(UtilityManager.TEMP_SAVE_FILENAME);
    }
    /**
     * Load the board manager from fileName.
     *
     * @param fileName the name of the file
     */
    private void loadFromFile(String fileName) {
        try{
            InputStream inputStream = this.openFileInput(fileName);
            if (inputStream != null) {
                ObjectInputStream input = new ObjectInputStream(inputStream);
                boardManager = (BoardManager) input.readObject();
                numRows = boardManager.getBoard().getNumRows();
                numColumns = boardManager.getBoard().getNumColumns();
                inputStream.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    /**
     * Save the board manager to fileName.
     *
     * @param fileName the name of the file
     */
    public void saveToFile(String fileName) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(
                    this.openFileOutput(fileName, MODE_PRIVATE));
            outputStream.writeObject(boardManager);
            outputStream.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    /**
     * Activate the undo button.
     */
    private void addUndoButtonListener() {
        Button undoButton = findViewById(R.id.UndoButton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boardManager.canUndo()){
                    boardManager.undo();
                    display();
                } else{
                    Context context = getApplicationContext();
                    makeCustomToastText(getString(R.string.ga_cannot_undo), context);
                }
            }
        });
    }

    /**
     * method to save board
     * @param v the view
     * @param isAutosave true if the game save method is autosave
     */
    public void onClickSaveBoard(View v, boolean isAutosave){
        if (boardIndex != -1) {
            boardList.set(boardIndex, boardManager);
        }
        if (isAutosave)
            makeCustomToastText(getString(R.string.ga_auto_saved), this);
        saveBoardsToAccounts(this, currentAccount, boardList);
    }

    /**
     * on click method to save board
     * @param v
     */
    public void onClickSaveBoard(View v){
        if(currentAccount == null)
            makeCustomToastText(getString(R.string.ga_guest_save), this);
        else {
            if (boardIndex != -1) {
                boardList.set(boardIndex, boardManager);
            }
            saveBoardsToAccounts(this, currentAccount, boardList);
            makeCustomToastText(getString(R.string.ga_manual_save), this);
        }
    }

    /**
     * start the chronometer for the game
     * @param chronometer the chronometer to start
     */
    public void startChronometer(Chronometer chronometer){
        if (!isChmRunning){
            chronometer.start();
            isChmRunning = true;
        }
    }

    /**
     * pause the chronometer for the game
     * @param chronometer the chronometer
     */
    public void pauseChronometer(Chronometer chronometer){
        if (isChmRunning){
            pauseTime = SystemClock.elapsedRealtime() - chronometer.getBase();
            chronometer.stop();
            isChmRunning = false;
        }
    }

    @Override
    public void onBackPressed() {
        IMAGE_SET = null;
        pauseChronometer(chronometer);
        if(boardManager!=null) {
            boardManager.setTimeSpent(pauseTime);
        }
        if(currentAccount != null) {
            onClickSaveBoard(getCurrentFocus(), false);
            if(timer != null && timerTask != null) {
                timer.cancel();
                timerTask.cancel();
            }
        }
        super.onBackPressed();
        finish();
    }

    /**
     * Set current account depending on different implementation of account loading
     * @param currentAccount current player's account
     */
    public void setCurrentAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
        TextView v = findViewById(R.id.text_currentUserGame);
        if(this.currentAccount != null) {
            v.setText(getString(R.string.ga_current_user, currentAccount.getUsername()));
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onClickSaveBoard(getCurrentFocus(),true);
                        }
                    });
                }
            };
            timer.scheduleAtFixedRate(timerTask, SAVE_INTERVAL, SAVE_INTERVAL);
        }
        else
            v.setText(getString(R.string.ga_guest_user));

    }
    @Override
    public void update(Observable o, Object arg) {
        display();
    }
}
