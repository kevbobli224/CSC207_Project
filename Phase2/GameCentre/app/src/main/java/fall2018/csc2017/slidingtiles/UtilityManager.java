package fall2018.csc2017.slidingtiles;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public final class UtilityManager {
    /**
     * A temporary save file.
     */
    public static final String TEMP_SAVE_FILENAME = "save_file_tmp.ser";
    /**
     * Account data storage file
     */
    public static final String ACCOUNTS_FILENAME = "account_file.ser";

    /**
     * Static function for loading a list of accounts
     *
     * @param ctx the current context
     * @return list of accounts
     */
    public static List<Account> loadAccountList(Context ctx) {
        List<Account> returnList = new ArrayList<>();
        try {
            InputStream inputStream = ctx.openFileInput(ACCOUNTS_FILENAME);
            if (inputStream != null) {
                ObjectInputStream input = new ObjectInputStream(inputStream);
                returnList = (List<Account>) input.readObject();
                inputStream.close();
                return returnList;
            }
        } catch (FileNotFoundException e) {
            Log.e("UM: loadAccountList", "File not found: " + e.toString());
            ObjectOutputStream os = new ObjectOutputStream(ctx.openFileOutput(ACCOUNTS_FILENAME, MODE_PRIVATE));
            os.writeObject(new ArrayList<Account>());
            os.close();
        } catch (IOException e) {
            Log.e("UM: loadAccountList", "Can not read file: " + e.toString());
        } catch (ClassNotFoundException e) {
            Log.e("UM: loadAccountList", "File contained unexpected data type: " + e.toString());
        } finally {
            return returnList;
        }
    }

    /**
     * Static function for saving a file for BoardManager
     *
     * @param fileName directory and name of the file
     * @param bm       the BoardManager to be saved
     * @param ctx      the current context
     */
    public static void saveBoardManagerToFile(String fileName, BoardManager bm, Context ctx) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(ctx.openFileOutput(fileName, MODE_PRIVATE));
            outputStream.writeObject(bm);
            outputStream.close();
        } catch (IOException e) {
            Log.e("UM: saveBoardManagerToFile.Exception", "File write failed: " + e.toString());
        }
    }

    /**
     * Static function for creating custom Toast messages
     *
     * @param displayText, the string to be displayed
     * @param ctx          the current context
     */
    public static void makeCustomToastText(String displayText, Context ctx) {
        if(ctx != null && ctx.getApplicationContext() != null)
            Toast.makeText(ctx, displayText, Toast.LENGTH_SHORT).show();
    }

    /**
     * Static function for saving boards to accounts
     *
     * @param ctx,      the current context
     * @param account   the current account to be saved in
     * @param boardList the list of boards that it will save
     */
    public static void saveBoardsToAccounts(Context ctx, Account account, List<BoardManager> boardList) {
        List<Account> accountList = new ArrayList<>();
        try {
            InputStream inputStream = ctx.openFileInput(ACCOUNTS_FILENAME);
            if (inputStream != null) {
                ObjectInputStream input = new ObjectInputStream(inputStream);
                accountList = (List<Account>) input.readObject();
                inputStream.close();
            }
            for (Account acc : accountList) {
                if (acc.equals(account)) {
                    acc.setBoardList((ArrayList<BoardManager>) boardList);
                }
            }
            ObjectOutputStream outputStream =
                    new ObjectOutputStream(ctx.openFileOutput(ACCOUNTS_FILENAME, MODE_PRIVATE));
            outputStream.writeObject(accountList);
            outputStream.close();
        } catch (FileNotFoundException e) {
            Log.e("UM: saveBoardsToAccounts", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("UM: saveBoardsToAccounts", "Can not read file: " + e.toString());
        } catch (ClassNotFoundException e) {
            Log.e("UM: saveBoardsToAccounts", "File contained unexpected data type: " + e.toString());
        }
    }

    /**
     * Static function for saving scores to accounts
     *
     * @param ctx,    the current context
     * @param account the current account to be saved in
     * @param score   the score of the game that will be save
     */
    public static void saveScoresToAccounts(Context ctx, Account account, int score) {
        List<Account> accountList = new ArrayList<Account>();
        try {
            InputStream inputStream = ctx.openFileInput(ACCOUNTS_FILENAME);
            if (inputStream != null) {
                ObjectInputStream input = new ObjectInputStream(inputStream);
                accountList = (List<Account>) input.readObject();
                inputStream.close();
            }
            for (Account acc : accountList) {
                if (acc.equals(account)) {
                    acc.addToSlidingGameScores(score);
                }
            }
            ObjectOutputStream outputStream =
                    new ObjectOutputStream(ctx.openFileOutput(ACCOUNTS_FILENAME, MODE_PRIVATE));
            outputStream.writeObject(accountList);
            outputStream.close();
        } catch (FileNotFoundException e) {
            Log.e("UM: saveScoresToAccounts", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("UM: saveScoresToAccounts", "Can not read file: " + e.toString());
        } catch (ClassNotFoundException e) {
            Log.e("UM: saveScoresToAccounts", "File contained unexpected data type: " + e.toString());
        }
    }

    /**
     * save function to save a new ultimate ttt game to the account
     *
     * @param ctx        the context
     * @param account    the account of the user
     * @param ultTTTSave the modified list of ultttt games
     */
    public static void saveUltTTTBoardManager(Context ctx, Account account, int ultTTTSave) {
        List<Account> accountList = new ArrayList<Account>();
        try {
            InputStream inputStream = ctx.openFileInput(ACCOUNTS_FILENAME);
            if (inputStream != null) {
                ObjectInputStream input = new ObjectInputStream(inputStream);
                accountList = (List<Account>) input.readObject();
                inputStream.close();
            }
            for (Account acc : accountList) {
                if (acc.equals(account)) {
                    acc.setUltimateTTTSave(ultTTTSave);
                }
            }
            ObjectOutputStream outputStream =
                    new ObjectOutputStream(ctx.openFileOutput(ACCOUNTS_FILENAME, MODE_PRIVATE));
            outputStream.writeObject(accountList);
            outputStream.close();
        } catch (FileNotFoundException e) {
            Log.e("UM: saveUltTTTBoardsToAccounts", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("UM: saveUltTTTBoardsToAccounts", "Can not read file: " + e.toString());
        } catch (ClassNotFoundException e) {
            Log.e("UM: saveUltTTTBoardsToAccounts", "File contained unexpected data type: " + e.toString());
        }
    }

    /**
     * Generates a dialog based on parameters
     *
     * @param title   the title the dialog will have
     * @param message the message of the dialog will have
     * @param ctx     the context of which the dialog will display on
     * @return An AlertDialog where it assembles all information provided
     */
    public static AlertDialog alertDialogBuilder(String title, String message, Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        AlertDialog ad = builder.create();
        if (title != null) {
            TextView titleText = new TextView(ctx);
            titleText.setText(title);
            titleText.setPadding(10, 10, 10, 10);
            titleText.setTextSize(30);
            titleText.setGravity(Gravity.CENTER);
            ad.setCustomTitle(titleText);
        } else
            ad.setTitle(" ");
        if (message != null) {
            ad.setMessage(message);
        } else
            ad.setMessage(" ");
        return ad;
    }

    /**
     * Generates a new random board with sizes based on parameters passed in
     *
     * @param rows    the new game's row properties
     * @param columns the new game's columns properties
     * @return A random board with specified dimensions
     */
    public static Board newRandomBoard(int rows, int columns) {
        List<Tile> tiles = new ArrayList<>();
        int numTiles = rows * columns;
        int checkSolvable = 0;
        int count = 0;
        for (int tileNum = 0; tileNum != numTiles; tileNum++) {
            tiles.add(new Tile(tileNum));
        }
        Collections.shuffle(tiles);
        while (true) {
            for (int tileNum = 0; tileNum < numTiles; tileNum++) {
                if (tiles.get(tileNum).getId() == numTiles) {
                    continue;
                }
                for (int x = tileNum + 1; x < numTiles; x++) {
                    if (tiles.get(tileNum).getId() == numTiles) {
                        continue;
                    }
                    if (tiles.get(tileNum).getId() > tiles.get(x).getId()) {
                        count++;
                    }
                }
                checkSolvable += count;
                count = 0;
            }
            if (checkSolvable % 2 == 0) {
                break;
            } else {
                Collections.shuffle(tiles);
                checkSolvable = 0;
            }
        }
        Board b = new Board(tiles, rows, columns);
        return b;
    }

    /**
     * Static function for saving scores to accounts
     *
     * @param ctx,    the current context
     * @param account the current account to be saved in
     * @param score   the score of the game that will be save
     */
    public static void saveObDodgerScoresToAccounts(Context ctx, Account account, int score) {
        List<Account> accountList = new ArrayList<>();
        try {
            InputStream inputStream = ctx.openFileInput(ACCOUNTS_FILENAME);
            if (inputStream != null) {
                ObjectInputStream input = new ObjectInputStream(inputStream);
                accountList = (List<Account>) input.readObject();
                inputStream.close();
            }
            for (Account acc : accountList) {
                if (acc.equals(account)) {
                    acc.addToObDodgeGameScores(score);
                }
            }
            ObjectOutputStream outputStream =
                    new ObjectOutputStream(ctx.openFileOutput(ACCOUNTS_FILENAME, MODE_PRIVATE));
            outputStream.writeObject(accountList);
            outputStream.close();
        } catch (FileNotFoundException e) {
            Log.e("UM: saveScoresToAccounts", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("UM: saveScoresToAccounts", "Can not read file: " + e.toString());
        } catch (ClassNotFoundException e) {
            Log.e("UM: saveScoresToAccounts", "File contained unexpected data type: " + e.toString());
        }
    }

    /**
     * Static function for saving scores to accounts
     *
     * @param ctx,    the current context
     * @param account the current account to be saved in
     * @param won   if the game was won
     */
    public static void saveUltTTTWinUpdate(Context ctx, Account account, Boolean won) {
        List<Account> accountList = new ArrayList<Account>();
        try {
            InputStream inputStream = ctx.openFileInput(ACCOUNTS_FILENAME);
            if (inputStream != null) {
                ObjectInputStream input = new ObjectInputStream(inputStream);
                accountList = (List<Account>) input.readObject();
                inputStream.close();
            }
            for (Account acc : accountList) {
                if (acc.equals(account)) {
                    acc.ultimateTTTWinUpdate(won);
                }
            }
            ObjectOutputStream outputStream =
                    new ObjectOutputStream(ctx.openFileOutput(ACCOUNTS_FILENAME, MODE_PRIVATE));
            outputStream.writeObject(accountList);
            outputStream.close();
        } catch (FileNotFoundException e) {
            Log.e("UM: saveScoresToAccounts", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("UM: saveScoresToAccounts", "Can not read file: " + e.toString());
        } catch (ClassNotFoundException e) {
            Log.e("UM: saveScoresToAccounts", "File contained unexpected data type: " + e.toString());
        }
    }
}
