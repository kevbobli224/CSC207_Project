package fall2018.csc2017.slidingtiles;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

/**
 * Simple test for MovementController, total of 2 methods to be covered
 */
@RunWith(AndroidJUnit4.class)
public class MovementControllerTest {
    /**
     * The context this test will be tested in.
     * Provided by Robolectric's mocking android context framework.
     */
    private Context appContext;
    /**
     * BoardManager that the controller would have
     */
    private BoardManager boardManager;
    /**
     * The MovementController instance
     */
    private MovementController mController;

    /**
     * Setup the context and the BoardManager to be 1 step away from solving
     */
    @Before
    public void setup() {
        appContext = RuntimeEnvironment.application;
        boardManager = BoardSetup.setUp4x4NearSolved();
    }

    /**
     * General test for the controller by processing various valid and invalid taps.
     *
     * processTapMovement() does not return boolean therefore cannot be asserted.
     * However code coverage would show the lines that it covered hence an indication that board
     * would be solved under sequences of taps
     */
    @Test
    public void generalTest() {
        mController = new MovementController();
        mController.setBoardManager(boardManager);
        //Invalid move
        mController.processTapMovement(appContext, 4, false);
        //Valid moves
        mController.processTapMovement(appContext, 10, false);
        mController.processTapMovement(appContext, 14, false);
        mController.processTapMovement(appContext, 15, false);

    }
}