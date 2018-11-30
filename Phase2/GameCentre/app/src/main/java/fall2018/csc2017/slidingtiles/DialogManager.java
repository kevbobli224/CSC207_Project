package fall2018.csc2017.slidingtiles;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;

import java.util.List;

import static fall2018.csc2017.slidingtiles.UtilityManager.makeCustomToastText;
import static fall2018.csc2017.slidingtiles.UtilityManager.newRandomBoard;
import static fall2018.csc2017.slidingtiles.UtilityManager.saveBoardsToAccounts;

/**
 * A popup dialog offering sliding tiles board options
 */
public class DialogManager implements PopupMenu.OnMenuItemClickListener{
    /**
     * the current activity
     */
    private final Activity currentActivity;
    /**
     * the dialog resource id
     */
    private int dialogResourceId;
    /**
     * the list of boards from the account
     */
    private List<BoardManager> boardList;
    /**
     * loader adapter for display
     */
    private LoaderAdapter adapter;
    /**
     * account of the player
     */
    private Account account;

    /**
     * Constructor for a new dialog manager
     * @param currentActivity the current activity to use the dialog manager in
     */
    public DialogManager(Activity currentActivity){
        this.currentActivity = currentActivity;
    }

    /**
     * Create a new dialog popup
     * @param popupResourceId the resource id
     * @param view the view
     * @param onClicker a listener for on click
     */
    public void createDialog(int popupResourceId, View view, PopupMenu.OnMenuItemClickListener onClicker){
        PopupMenu popupMenu = new PopupMenu(currentActivity, view);
        popupMenu.setOnMenuItemClickListener(onClicker);
        popupMenu.inflate(popupResourceId);
        popupMenu.show();
    }

    /**
     * Make a board of the selected option
     * @param dialogPosition the position of the selected board option
     */
    public void generateBoard(int dialogPosition){
        switch (dialogPosition){
            case 1:
                boardList.add(new BoardManager(newRandomBoard(3,3)));
                adapter.notifyDataSetChanged();
                saveBoardsToAccounts(currentActivity, account, boardList);
                makeCustomToastText("3x3", currentActivity);
                break;
            case 2:
                boardList.add(new BoardManager(newRandomBoard(4,4)));
                adapter.notifyDataSetChanged();
                saveBoardsToAccounts(currentActivity, account, boardList);
                makeCustomToastText("4x4", currentActivity);
                break;
            case 3:
                boardList.add(new BoardManager(newRandomBoard(3,3)));
                adapter.notifyDataSetChanged();
                saveBoardsToAccounts(currentActivity, account, boardList);
                makeCustomToastText("5x5", currentActivity);
                break;
        }
    }

    /**
     * set the layout of the dialog
     * @param dialogResourceId the dialog resource id
     */
    public void setDialogLayout(int dialogResourceId){
        this.dialogResourceId = dialogResourceId;
    }

    /**
     * set up all the components of this dialog manager
     * @param boardList the list of boards for the account
     * @param adapter the loader adapter
     * @param account the account
     */
    public void setupComponents(List<BoardManager> boardList, LoaderAdapter adapter, Account account){
        this.boardList = boardList;
        this.adapter = adapter;
        this.account = account;
    }

    /**
     * Checking that all inputs are filled
     * @param row the input for the number of rows of a new board
     * @param column the input for the columns of a new board
     * @return whether the user has filled out all the information needed to make a new board
     */
    private boolean checkValidDialogInputs(String row, String column){
        if (row.equals("")||column.equals("")) {
            makeCustomToastText(currentActivity.getString(R.string.d_toast_empty_fields), currentActivity);
            return false;
        } else if (Integer.parseInt(row) < 3 || Integer.parseInt(column) < 3){
            makeCustomToastText(currentActivity.getString(R.string.d_toast_let_3), currentActivity);
            return false;
        } else if (Integer.parseInt(row) > 31 || Integer.parseInt(column) > 31){
            makeCustomToastText(currentActivity.getString(R.string.d_toast_lat_31), currentActivity);
            return false;
        }
        return true;
    }

    /**
     * Implements an click listener for choosing particular menu items
     * @param item the id of items assigned in the res/menu/menu_sliding_difficulty
     * @return returns the boolean whether a valid click had occurred.
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int dialogPosition = item.getItemId();
        if(dialogPosition == R.id.item1) {
            generateBoard(1);
            return true;
        }
        else if(dialogPosition == R.id.item2) {
            generateBoard(2);
            return true;
        }
        else if(dialogPosition == R.id.item3){
            generateBoard(3);
            return true;
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
            LayoutInflater inflater = currentActivity.getLayoutInflater();
            builder.setView(inflater.inflate(dialogResourceId, null));
            final Dialog dialog = builder.create();
            dialog.show();

            Button confirmButton = dialog.findViewById(R.id.button_confirm_difficulty);
            Button loadImageButton = dialog.findViewById(R.id.button_loadImage);
            final ImageView imagePreview = dialog.findViewById(R.id.iv_preview);
            final EditText rows = dialog.findViewById(R.id.text_row);
            final EditText columns = dialog.findViewById(R.id.text_column);
            final EditText undos = dialog.findViewById(R.id.text_undos);
            final EditText etUrl = dialog.findViewById(R.id.et_Url);
            final ImageResultReceiver resultReceiver = new ImageResultReceiver(new Handler(), imagePreview);
            resultReceiver.setReceiver(resultReceiver);

            loadImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = etUrl.getText().toString();
                    Intent imageIntent = new Intent(v.getContext(), ImageServiceIntent.class);
                    imageIntent.putExtra("receiver", resultReceiver);
                    imageIntent.putExtra("url", url);
                    imageIntent.putExtra("rows", 0);
                    imageIntent.putExtra("columns", 0);
                    currentActivity.startService(imageIntent);
                    if(resultReceiver.contentReceived())
                        makeCustomToastText(currentActivity.getString(R.string.d_toast_succ_load), currentActivity);
                    else if (resultReceiver.invalidImageLink())
                        makeCustomToastText(currentActivity.getString(R.string.d_toast_invalid_url), currentActivity);
                    else if (!resultReceiver.resultReceiverInitialized())
                        makeCustomToastText(currentActivity.getString(R.string.d_toast_no_image), currentActivity);
                    else
                        makeCustomToastText(currentActivity.getString(R.string.d_toast_racecar), currentActivity);
                }
            });
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox useImage = dialog.findViewById(R.id.cb_useImage);
                    int parsedUndo = undos.getText().toString().equals("") ? 0 : Integer.parseInt(undos.getText().toString());
                    if(useImage.isChecked()){
                        if(resultReceiver.contentReceived() && checkValidDialogInputs(rows.getText().toString(),columns.getText().toString() )){
                            int rowsInt = Integer.parseInt(rows.getText().toString());
                            int colsInt = Integer.parseInt(columns.getText().toString());
                            BoardManager bm = new BoardManager(newRandomBoard(rowsInt, colsInt));
                            bm.setCustomImageSet(resultReceiver.getBitmapList(
                                    ImageServiceIntent.bitmapSplitter
                                    (resultReceiver.getUnprocessedBitmap(), rowsInt , colsInt)));
                            bm.setNumCanUndo(parsedUndo);
                            bm.setUseImage(true);
                            boardList.add(bm);
                            adapter.notifyDataSetChanged();
                            saveBoardsToAccounts(currentActivity, account, boardList);
                            makeCustomToastText(rowsInt + "x" +colsInt, currentActivity);
                            dialog.dismiss();
                    } else if (resultReceiver.invalidImageLink()) {
                        makeCustomToastText(currentActivity.getString(R.string.d_toast_fix_url), currentActivity);
                    } else {
                        makeCustomToastText(currentActivity.getString(R.string.d_toast_wait_url), currentActivity);
                    }
                    } else if (checkValidDialogInputs(rows.getText().toString(), columns.getText().toString())) {
                        Board randomBoard = newRandomBoard(Integer.parseInt(rows.getText().toString()),
                                Integer.parseInt(columns.getText().toString()));
                        BoardManager bm = new BoardManager(randomBoard);
                        bm.setNumCanUndo(parsedUndo);
                        boardList.add(bm);
                        adapter.notifyDataSetChanged();
                        saveBoardsToAccounts(currentActivity, account, boardList);
                        makeCustomToastText(randomBoard.getTilesDimension(), currentActivity);
                        dialog.dismiss();
                    }
                }
            });
            return true;
        }
    }
}
