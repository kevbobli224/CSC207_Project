package fall2018.csc2017.slidingtiles.UltimateTTT;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import fall2018.csc2017.slidingtiles.Account;
import fall2018.csc2017.slidingtiles.GameSelection;
import fall2018.csc2017.slidingtiles.R;
import fall2018.csc2017.slidingtiles.ScoreBoard;

import fall2018.csc2017.slidingtiles.UtilityManager;

/**
 * Adapted from: https://github.com/Prakash2403/UltimateTicTacToe/blob/master/app/src/main/java/com/example/prakash/ultimatetictactoe/frontend/Fifth.java
 */

public class UltimateTTTGameActivity extends AppCompatActivity implements View.OnClickListener
{
    private UltTTTBoardManager ultTTTBoardManager;
    private UltTTTConnector connector;
    private ImageButton[] ImageButtons;
    private TableLayout tables[];

    private Account currentAccount;
    private boolean IS_GUEST = false;

    String P1Name;
    String P2Name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_uttt_main);

        connector = new UltTTTConnector(this);
        ImageButtons = connector.getImageButtons();
        tables = connector.getTables();
        initialize();

        if (GameSelection.IS_GUEST) {
            IS_GUEST = true;
            P1Name = "Guest1";
            P2Name = "Guest2";
        } else {
            currentAccount = (Account) getIntent().getSerializableExtra("account");
            P1Name = currentAccount.getUsername();
            P2Name = "Guest";
        }
    }


    public void initialize() {
        connector.scoreP1.setText("0");
        connector.scoreP2.setText("0");
        for (ImageButton ImageButton : ImageButtons) {
            ImageButton.setOnClickListener(this);
            ImageButton.setBackgroundResource(R.drawable.ult_clearimage);
        }
        for (TableLayout tableLayout : tables)
            tableLayout.setBackgroundColor(Color.BLACK);
        connector.breset.setOnClickListener(this);
        connector.breset.setEnabled(true);
        connector.bundo.setOnClickListener(this);
        connector.bundo.setEnabled(true);
    }

    public void enableAll() {
        for (android.widget.ImageButton ImageButton : ImageButtons) ImageButton.setEnabled(true);
    }

    public void disableAll() {
        for (ImageButton ImageButton : ImageButtons) ImageButton.setEnabled(false);
    }

    public void enable(int id) {
        ImageButtons[id].setEnabled(true);
    }

    public void disable(int id) {
        ImageButtons[id].setEnabled(false);
    }


    @Override
    public void onClick(View v) {
        int index;
        ImageButton curr_button;
        curr_button = findViewById(v.getId());
        index = getIndex(curr_button);
        runFrontEnd(index);
        ultTTTBoardManager.operate();
    }

    public void runFrontEnd(int index) {
        JSONObject response;
        response = connector.backend.execute(index);
        ultTTTBoardManager = new UltTTTBoardManager(UltimateTTTInfoManager.parseJson(response), connector);
        if (!IS_GUEST) {
            UtilityManager.saveUltimateTTTUltTTTBoardManager(this, currentAccount, currentAccount.getUltimateTTTList());
        }
    }

    public void setText(TextView tv, String s) {
        tv.setText(s);
    }

    public void disableUsedCells(String[] used_cells) {
        for (String used_cell : used_cells) {
            try {
                disable(Integer.parseInt(used_cell));
            } catch (NumberFormatException e) {

            }
        }
    }

    public void disableWinnerBlocks(String disableBlock) {
        for (int i = 0; i < disableBlock.length(); i++)
            disableBlock(disableBlock.charAt(i) - 48);
    }

    public void enableBlock(int nextActiveBlock) {
        if (nextActiveBlock != Integer.MAX_VALUE)
            for (int i = nextActiveBlock * 9; i < (nextActiveBlock + 1) * 9; i++)
                enable(i);
        else {
            enableAll();
        }
    }

    public void disableBlock(int id) {
        for (int i = id * 9; i < (id + 1) * 9; i++)
            disable(i);
    }

    public String getGlobalWinnerName(String global_winner) {
        if (global_winner.equals("Player 1"))
            return P1Name + " wins";
        else if (global_winner.equals("Player 2"))
            return P2Name + " wins";
        else if (global_winner.equals("Drawn"))
            return "Match Drawn";
        return "None";
    }

    public void showToast(String winner) {
        Toast.makeText(UltimateTTTGameActivity.this, winner, Toast.LENGTH_SHORT).show();
    }

    private int getIndex(ImageButton b) {
        for (int i = 0; i < ImageButtons.length; i++) {
            if (ImageButtons[i].equals(b)) {
                return i;
            }
        }
        if (b.equals(connector.breset))
            return 100;
        if (b.equals(connector.bundo))
            return 200;
        return -1;
    }
}
