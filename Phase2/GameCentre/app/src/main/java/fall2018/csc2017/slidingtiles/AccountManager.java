package fall2018.csc2017.slidingtiles;

import android.app.Activity;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static fall2018.csc2017.slidingtiles.UtilityManager.makeCustomToastText;

/**
 * Manage an account, including creating, saving and getting account information.
 */
public class AccountManager {
    /**
     * List of account's
     */
    private List<Account> accountsList;
    /**
     * The current account
     */
    private Account currentAccount;

    /**
     * Manage a list of accounts.
     *
     * @param accountsList the list of accounts
     */
    public AccountManager(List<Account> accountsList) {
        this.accountsList = accountsList;
    }

    /**
     * Checks if account already exists
     *
     * @param username the account to be checked
     * @return whether if account already exists
     */
    boolean checkExistingUser(String username){
        if(accountsList == null || accountsList.isEmpty())
            return false;
        for (Account existingAccount : accountsList) {
            if (username.equals(existingAccount.getUsername()))
                return true;
        }
        return false;
    }

    /**
     * Sets the account lists based on different implementation of loading accountsList
     * Part of dependency-injection usage.
     * @param accountsList the list of accounts
     */
    void setAccountsList(List<Account> accountsList) {
        this.accountsList = accountsList;
    }

    /**
     * Retrieves the current accounts lists
     * @return List<Account> the list of accounts.
     */
    List<Account> getAccountsList(){
        return accountsList;
    }

    /**
     * Authenticates the user with it's corresponding login details input
     *
     * @param username the username to be checked
     * @return whether if input credentials match
     */
    boolean authenticateCredentials(String username, String password) {
        for (Account acc : accountsList) {
            if (username.equals(acc.getUsername()) && password.equals(acc.getPassword())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the account according to username
     *
     * @param name the username used to get account
     * @return the account of the username
     */
    Account getAccountFromUsername(String name){
        if(accountsList == null || accountsList.isEmpty())
            return null;
        for(Account acc: accountsList)
        {
            if(name.equals(acc.getUsername())){
                return acc;
            }
        }
        return null;
    }

    /**
     * Gets the current accounts' board list
     */
    List<BoardManager> getCurrentAccountBoardList(Account account, boolean guest) {
        List<BoardManager> bm;
        bm = guest ? new ArrayList<BoardManager>() :
                account != null ? account.getBoardList() : new ArrayList<BoardManager>();
        return bm;
    }

    /**
     * Creates a new account based on requirements
     *
     * @param username the username that the account to be created with
     * @param password the password that associates to the account
     * @param activity the activity this manager is in for toast messages
     * @return A new account, null if invalid credentials or existing account
     */
    Account createNewAccount(String username, String password, Activity activity) {
        Account account;
        if(username.equals("Guest") || username.equals("guest")){
            makeToastMessage(ToastConstant.TOAST_RESERVED, activity);
        } else if (username.equals("") || username.equals("")){
            makeToastMessage(ToastConstant.TOAST_EMPTY, activity);
        } else if (username.length() < 3 || password.length() < 3 ) {
            makeToastMessage(ToastConstant.TOAST_LEAST, activity);
        } else if (!checkExistingUser(username)){
            account = new Account(username, password);
            accountsList.add(account);
            makeToastMessage(ToastConstant.TOAST_SUCCESS, activity);
            return account;
        }
        else
            makeToastMessage(ToastConstant.TOAST_EXIST, activity);
        return null;
    }

    /**
     * ToastConstant for assigning cases of toasts messages
     */
    public enum ToastConstant{
        TOAST_RESERVED,
        TOAST_EMPTY,
        TOAST_LEAST,
        TOAST_EXIST,
        TOAST_SUCCESS,
        TOAST_ERROR_SAVE
    }

    /**
     * Makes custom toast message based on different ToastConstant. This allows checking for
     * null activity yet allows account creating and saving without relying on a valid activity.
     * @param toastMessage ToastConstant that refers to different toast message to be shown
     * @param activity activity that toast would show in
     */
    private void makeToastMessage(ToastConstant toastMessage, Activity activity){
        if(activity == null)
            return;
        switch (toastMessage){
            case TOAST_EMPTY:
                makeCustomToastText(activity.getString(R.string.am_empty_field), activity);
                break;
            case TOAST_EXIST:
                makeCustomToastText(activity.getString(R.string.am_existing_user), activity);
                break;
            case TOAST_LEAST:
                makeCustomToastText(activity.getString(R.string.am_invalid_field), activity);
                break;
            case TOAST_RESERVED:
                makeCustomToastText(activity.getString(R.string.am_guest_reserved), activity);
                break;
            case TOAST_SUCCESS:
                makeCustomToastText(activity.getString(R.string.am_register_succ), activity);
                break;
            case TOAST_ERROR_SAVE:
                makeCustomToastText(activity.getString(R.string.am_error_save), activity);
                break;
        }
    }
    /**
     * Saves current list of accounts to fileName
     *
     * @param fileName        the name of the file
     * @param currentActivity the activity this manager is in for openFileOutput() accessing.
     */
    public void saveCredentials(String fileName, Activity currentActivity){
        try{
            ObjectOutputStream outputStream;
            if(currentActivity == null || currentActivity.getApplicationContext() == null) {
                outputStream = new ObjectOutputStream(new FileOutputStream(fileName));
            }else {
                outputStream = new ObjectOutputStream(currentActivity.openFileOutput(fileName, MODE_PRIVATE));
            }
            outputStream.writeObject(accountsList);
            outputStream.close();
        } catch (Exception e){
            makeToastMessage(ToastConstant.TOAST_ERROR_SAVE, currentActivity);
        }
    }
}
