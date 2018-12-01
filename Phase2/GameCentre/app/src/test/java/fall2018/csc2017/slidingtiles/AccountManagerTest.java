package fall2018.csc2017.slidingtiles;

import android.app.Activity;
import android.content.Intent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static fall2018.csc2017.slidingtiles.UtilityManager.ACCOUNTS_FILENAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountManagerTest {
    private List<Account> testAccountList = new ArrayList<>();
    private Account firstAccount = new Account("123","123") ;
    private AccountManager am;
    @Before
    public void setUp(){
        testAccountList.add(firstAccount);
        am = new AccountManager(testAccountList);
    }

    @Test
    public void checkExistingUser() {
        assertTrue(am.checkExistingUser("123"));
        assertFalse(am.checkExistingUser("bogus"));
        List<Account> emptyAccounts = new ArrayList<>();
        am.setAccountsList(emptyAccounts);
        assertFalse(am.checkExistingUser("123"));
        am.setAccountsList(null);
        assertFalse(am.checkExistingUser("123"));
    }

    @Test
    public void setAccountsList() {
        List<Account> newList = new ArrayList<>();
        am.setAccountsList(newList);
        assertEquals(am.getAccountsList(), newList);
        am.setAccountsList(null);
        assertNull(am.getAccountsList());
    }

    @Test
    public void authenticateCredentials() {
        assertTrue(am.authenticateCredentials("123", "123"));
        assertFalse(am.authenticateCredentials("1234", "1234"));
    }

    @Test
    public void getAccountFromUsername() {
        assertEquals(firstAccount, am.getAccountFromUsername("123"));
        assertNull(am.getAccountFromUsername("bogus"));
        am.setAccountsList(null);
        assertNull(am.getAccountFromUsername("123"));
    }

    @Test
    public void getCurrentAccountBoardList() {
        List<BoardManager> boardList = new ArrayList<>();
        boardList.add(BoardSetup.setUp4x4Solved());
        firstAccount.setBoardList(boardList);
        assertNotNull(am.getCurrentAccountBoardList(firstAccount, false));
        assertEquals(new ArrayList<BoardManager>(), am.getCurrentAccountBoardList(firstAccount, true));
        assertEquals(new ArrayList<BoardManager>(), am.getCurrentAccountBoardList(null, true));
    }

    @Test
    public void createNewAccount() {
        Activity dummyActivity = new Activity();
        assertNull(am.createNewAccount("Guest", "1234", dummyActivity));
        assertNull(am.createNewAccount("Guest", "1234", null));
        assertNull(am.createNewAccount("1234", "", dummyActivity));
        assertNull(am.createNewAccount("", "1234", dummyActivity));
        assertNull(am.createNewAccount("12", "12", dummyActivity));
        assertNull(am.createNewAccount("12", "1234", dummyActivity));
        assertNull(am.createNewAccount("1234", "12", dummyActivity));
        assertNull(am.createNewAccount("12", "12", dummyActivity));
        assertNull(am.createNewAccount("123", "123", dummyActivity));
        assertTrue(am.createNewAccount("validusername", "12345", dummyActivity)
                .equals(new Account("validusername", "12345")));
    }

    @Rule
    public ExpectedException e = ExpectedException.none();

    @Test
    public void saveCredentials() {
        Activity dummyActivity = new Activity();
        dummyActivity.setIntent(new Intent(dummyActivity, LaunchCentre.class));
        am.saveCredentials("bogus", dummyActivity);
        am.saveCredentials(null, null);
        am.saveCredentials(ACCOUNTS_FILENAME, null);
        e.expect(Exception.class);
        AccountManager ma = mock(AccountManager.class);
        doThrow(Exception.class).when(ma).saveCredentials((String) isNull(), (Activity) isNull());
        ma.saveCredentials(null, null);
        verify(ma, times(1)).saveCredentials(null,null);
    }
}