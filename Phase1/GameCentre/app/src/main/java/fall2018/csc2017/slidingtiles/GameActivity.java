package fall2018.csc2017.slidingtiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
     * The current user's username
     */
    private String currentUsername;

    /**
     * Constants for swiping directions. Should be an enum, probably.
     */
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;

    public static final int SAVE_INTERVAL = 10000;

    // Grid View and calculated column height and width based on device size
    private GestureDetectGridView gridView;
    private static int columnWidth, columnHeight;
    private Account currentAccount;
    private ArrayList<BoardManager> boardList;
    private int boardIndex;
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private Button undoButton;
    private Integer currentScore;
    private int numRows, numColumns;
    private final Context ctx = this;
    public static ArrayList<Bitmap> IMAGE_SET;

    /**
     * Set up the background image for each button based on the master list
     * of positions, and then call the adapter to set the view.
     */
    // Display
    public void display() {
        updateTileButtons(boardManager.isUseImage());
        gridView.setAdapter(new CustomAdapter(tileButtons, columnWidth, columnHeight));
        if (boardManager.puzzleSolved()) {
            timer.cancel();
            timerTask.cancel();
            currentScore = -100;//TODO: remove, it's not a real score
            if(!GameSelection.IS_GUEST)
                currentAccount.addToSlidingGameScores(10); //TODO: add the sliding game score
            gridView = findViewById(R.id.grid);
            Intent tmp = new Intent(gridView.getContext(), ScoreBoard.class);
            if(!GameSelection.IS_GUEST) {
                tmp.putExtra("currentUsername", currentAccount.getUsername());
                tmp.putExtra("board", boardManager.getBoard());
            }
            else {
                tmp.putExtra("currentUsername", "-1");
            }
            tmp.putExtra("currentScore", currentScore.toString()); //TODO: pass the current score
            startActivity(tmp);
            finish();
        }
        undoButton = findViewById(R.id.UndoButton);
        undoButton.setText("Undo:"+boardManager.getNumCanUndo());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadFromFile(TEMP_SAVE_FILENAME);
        currentAccount = (Account) getIntent().getSerializableExtra("account");
        boardList = (ArrayList<BoardManager>) getIntent().getSerializableExtra("boardList");
        boardIndex = this.getIntent().getIntExtra("boardIndex", -1);
        setContentView(R.layout.activity_main);
        boardManager.setUseImage(IMAGE_SET!=null);
        if(boardManager.isUseImage()){
            boardManager.setCustomImageSet(IMAGE_SET);
        }
        addUndoButtonListener();
        TextView v = findViewById(R.id.text_currentUserGame);
        if(!GameSelection.IS_GUEST)
            v.setText(currentAccount.getUsername());
        else
            v.setText("Guest");

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
                        createTileButtons(ctx, boardManager.isUseImage());
                        display();
                    }
                });

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
    public Drawable generateBackgroundTiles(int id, boolean blank, Tile t){
        if(blank)
        {
            if(!t.hasBackground())
                t.setBackground(getDrawable(R.drawable.bg_simplebg));
            return getDrawable(R.drawable.bg_simplebg);
        }
        else if(id < 10){
            LayerDrawable ld = generateTileLayers(DigitEnum.DIGIT_ONES, id);
            if(!t.hasBackground())
                t.setBackground(ld);
            return ld;
        }
        else if (id < 100){
            LayerDrawable ld = generateTileLayers(DigitEnum.DIGIT_TENS, id);
            if(!t.hasBackground())
                t.setBackground(ld);
            return ld;
        } else if (id < 1000)
        {
            LayerDrawable ld = generateTileLayers(DigitEnum.DIGIT_THOU, id);
            if(!t.hasBackground())
                t.setBackground(ld);
            return ld;
        }
        return null;
    }
    public enum DigitEnum
    {
        DIGIT_ONES, DIGIT_TENS, DIGIT_THOU
    }
    public LayerDrawable generateTileLayers(DigitEnum digitEnum, int id)
    {
        Drawable bg = getDrawable(R.drawable.bg_simplebg);
        switch (digitEnum) {
            case DIGIT_ONES:
                int onesNumberPath = this.getResources().getIdentifier("ic_"+Integer.toString(id), "drawable", getPackageName());
                Drawable onesDrawable = getDrawable(onesNumberPath);
                LayerDrawable ld = new LayerDrawable(new Drawable[]{bg,onesDrawable});
                ld.setLayerInset(1, columnWidth/4,0,0,30);
                ld.setLayerWidth(1, columnWidth/2);
                return ld;
            case DIGIT_TENS:
                String tensString = Integer.toString(id).substring(0,1);
                String onesString = Integer.toString(id).substring(1);
                int onesNumberPath2 = this.getResources().getIdentifier("ic_"+onesString, "drawable", getPackageName());
                int tensNumberPath2 = this.getResources().getIdentifier("ic_"+tensString, "drawable", getPackageName());
                Drawable onesDrawable2 = getDrawable(onesNumberPath2);
                Drawable tensDrawable = getDrawable(tensNumberPath2);
                LayerDrawable ld2 = new LayerDrawable(new Drawable[]{bg,tensDrawable, onesDrawable2});
                ld2.setLayerWidth(1, columnWidth/2 - 5);
                ld2.setLayerWidth(2, columnWidth/2 - 5);
                ld2.setLayerInset(1,0,0,0,30);
                ld2.setLayerInset(2, columnWidth/2,0,0,30);
                return ld2;
            case DIGIT_THOU:
                String thouString3 = Integer.toString(id).substring(0,1);
                String tensString3 = Integer.toString(id).substring(1,2);
                String onesString3 = Integer.toString(id).substring(2);
                int thouNumberPath3 = this.getResources().getIdentifier("ic_"+thouString3, "drawable", getPackageName());
                int tensNumberPath3 = this.getResources().getIdentifier("ic_"+tensString3, "drawable", getPackageName());
                int onesNumberPath3 = this.getResources().getIdentifier("ic_"+onesString3, "drawable", getPackageName());
                Drawable onesDrawable3 = getDrawable(onesNumberPath3);
                Drawable tensDrawable3 = getDrawable(tensNumberPath3);
                Drawable thouDrawable3 = getDrawable(thouNumberPath3);
                LayerDrawable ld3 = new LayerDrawable(new Drawable[]{bg,thouDrawable3,tensDrawable3, onesDrawable3});
                ld3.setLayerWidth(1, columnWidth/3 - 5);
                ld3.setLayerWidth(2, columnWidth/3 - 5);
                ld3.setLayerWidth(3, columnWidth/3 - 5);
                ld3.setLayerInset(1,0,0,0,30);
                ld3.setLayerInset(2, columnWidth/3,0,0,30);
                ld3.setLayerInset(3, 2*columnWidth/3,0,0,30);
                return ld3;
        }
        return null;
    }
    public Drawable generateImageTiles(Tile t, @Nullable Bitmap b, boolean blank){
        if(blank)
        {
            if(!t.hasBackground())
                t.setBackground(getDrawable(R.drawable.bg_simplebg));
            return getDrawable(R.drawable.bg_simplebg);
        }else {
            Drawable bg = new BitmapDrawable(getResources(), b);
            if (!t.hasBackground())
                t.setBackground(bg);
            return bg;
        }
    }
    /**
     * Create the buttons for displaying the tiles.
     *
     * @param context the context
     */
    private void createTileButtons(Context context, boolean useImage) {
        Board board = boardManager.getBoard();
        numColumns = board.numColumns;
        numRows = board.numRows;
        tileButtons = new ArrayList<>();
        for (int row = 0; row != numRows; row++) {
            for (int col = 0; col != numColumns; col++) {
                Button tmp = new Button(context);
                if(useImage){
                    tmp.setBackground(generateImageTiles(board.getTile(row,col), boardManager.getCustomImageSet().get(board.getTile(row,col).getId()-1), false));
                }
                else if(board.getTile(row,col).getId() < numColumns*numRows)
                    tmp.setBackground(generateBackgroundTiles(board.getTile(row,col).getId(),false, board.getTile(row,col) ));
                else
                    tmp.setBackground(generateBackgroundTiles(board.getTile(row,col).getId(),true, board.getTile(row,col)));

                this.tileButtons.add(tmp);
            }
        }
    }
    /**
     * Update the backgrounds on the buttons to match the tiles.
     */
    private void updateTileButtons(boolean useImage) {
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
        saveToFile(StartingActivity.TEMP_SAVE_FILENAME);
    }
    /**
     * Load the board manager from fileName.
     *
     * @param fileName the name of the file
     */
    private void loadFromFile(String fileName) {

        try {
            InputStream inputStream = this.openFileInput(fileName);
            if (inputStream != null) {
                ObjectInputStream input = new ObjectInputStream(inputStream);
                boardManager = (BoardManager) input.readObject();
                numRows = boardManager.getBoard().numRows;
                numColumns = boardManager.getBoard().numColumns;
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        } catch (ClassNotFoundException e) {
            Log.e("login activity", "File contained unexpected data type: " + e.toString());
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
                    Toast.makeText(context, "Not Able To Undo", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void onClickSaveBoard(View v, boolean isAutosave){
        if(GameSelection.IS_GUEST)
            makeCustomToastText("Cannot save as guest!", this);
        else {
            if (boardIndex != -1) {
                boardList.set(boardIndex, boardManager);
            }
            saveBoardsToAccounts(this, currentAccount, boardList);
            if (isAutosave)
                makeCustomToastText("Auto-saved!", this);
        }
    }
    public void onClickSaveBoard(View v){
        if(GameSelection.IS_GUEST)
            makeCustomToastText("Cannot save as guest!", this);
        else {
            if (boardIndex != -1) {
                boardList.set(boardIndex, boardManager);
            }
            saveBoardsToAccounts(this, currentAccount, boardList);
            makeCustomToastText("Saved!", this);
        }
    }
    @Override
    public void onBackPressed() {
        onClickSaveBoard(getCurrentFocus(), false);
        timer.cancel();
        timerTask.cancel();
        super.onBackPressed();
        finish();
    }
    @Override
    public void update(Observable o, Object arg) {
        display();
    }
}
