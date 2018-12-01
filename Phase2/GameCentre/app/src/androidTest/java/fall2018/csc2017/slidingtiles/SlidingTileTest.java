package fall2018.csc2017.slidingtiles;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.Root;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.espresso.util.TreeIterables;
import android.support.test.filters.SmallTest;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static android.content.Context.MODE_PRIVATE;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasChildCount;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static fall2018.csc2017.slidingtiles.UtilityManager.ACCOUNTS_FILENAME;
import static fall2018.csc2017.slidingtiles.UtilityManager.saveBoardManagerToFile;
import static fall2018.csc2017.slidingtiles.UtilityManager.saveBoardsToAccounts;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

@RunWith(JUnit4.class)
@SmallTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SlidingTileTest {
    /**
     * BoardManager that sets up a nearly solved 4x4 board
     */
    private BoardManager board4x4 = BoardSetupInstrumented.setUp4x4NearSolved();
    /**
     * The activity that each test cycle will store
     */
    private Activity activity;
    /**
     * Overriding certain lifecycle of IntentsTestRule for relaying data that requires previous
     * intent to pass in.
     */
    @Rule
    public IntentsTestRule<GameActivity> testRule = new IntentsTestRule<GameActivity>(GameActivity.class){
        /**
         * Ensure that the board is 1 move away from solving by swapping the last tile.
         * Also saves board manager to a isolated file system for scoreboard retrieval.
         */
        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();
            GameSelection.IS_GUEST = false;
            List<Account> accountsList = new ArrayList<>();
            accountsList.add(new Account("123", "123"));
            try {
            ObjectOutputStream outputStream =
                    new ObjectOutputStream(InstrumentationRegistry.getTargetContext().openFileOutput(ACCOUNTS_FILENAME, MODE_PRIVATE));
            outputStream.writeObject(accountsList);
            outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            saveBoardManagerToFile(UtilityManager.TEMP_SAVE_FILENAME,
                    board4x4, InstrumentationRegistry.getTargetContext());
            saveBoardsToAccounts(InstrumentationRegistry.getTargetContext(), new Account("123","123"), new ArrayList<BoardManager>());
        }

        /**
         * Overriding the intent creation cycle
         * @return Intent that has all the data from required previous intent
         */
        @Override
        protected Intent getActivityIntent() {
            Intent intent = new Intent();
            List<BoardManager> boardManagerList = new ArrayList<>();
            boardManagerList.add(board4x4);

            intent.putExtra("account", new Account("123","123"));
            intent.putExtra("boardIndex", 0);
            intent.putExtra("boardList", (ArrayList)boardManagerList);

            return intent;
        }
        /**
         * Initializes activity after activity has launched
         */
        @Override
        protected void afterActivityLaunched() {
            activity = getActivity();
            super.afterActivityLaunched();

        }
    };

    /**
     * Custom ViewAction for waiting on a certain view component, primarily used for debugging
     * the view component.
     * @param viewId the view id of a view widget/component displayed on the screen
     * @param seconds milliseconds of delay to be performed
     * @return ViewAction that is performable by a runner
     */
    private static ViewAction delayFor(final int viewId, final long seconds)
    {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "waiting";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + seconds;
                final Matcher<View> viewMatcher = withId(viewId);
                do {
                    for (View childView : TreeIterables.breadthFirstViewTraversal(view)) {
                        if (viewMatcher.matches(childView)) {
                            return;
                        }
                    }
                    uiController.loopMainThreadForAtLeast(50);
                }
                while (System.currentTimeMillis() < endTime);

            }
        };
    }

    /**
     * Wait until certain text field ends with an input targetString
     * @param viewId the view to be matched against
     * @param targetString the suffix which the string should end it
     * @param timeoutThreshold the threshold that this ViewAction will run for
     * @return ViewAction that is performable
     */
    private static ViewAction waitUntil(final int viewId,
                                        final long timeoutThreshold,
                                        final String... targetString){
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return instanceOf(TextView.class);
            }

            @Override
            public String getDescription() {
                return "Waiting until string matches target string: " + targetString;
            }

            @Override
            public void perform(UiController uiController, View view) {
                boolean reached = false, foundView = false;
                uiController.loopMainThreadUntilIdle();
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + timeoutThreshold;
                final Matcher<View> viewMatcher = withId(viewId);
                TextView textfield = null;
                do {
                    if(!foundView) {
                        for (View childView : TreeIterables.breadthFirstViewTraversal(view)) {
                            if (viewMatcher.matches(childView)) {
                                foundView = true;
                                textfield = (TextView) childView;
                            }
                        }
                    }
                    else{
                        if(System.currentTimeMillis() >= endTime) {
                            throw new PerformException.Builder()
                                    .withActionDescription(this.getDescription())
                                    .withViewDescription(HumanReadables.describe(view))
                                    .withCause(new TimeoutException())
                                    .build();
                        }
                        else if(textfield != null) {
                            if(textfield.getText().toString().endsWith(targetString[0]) ||
                                    textfield.getText().toString().endsWith(targetString[1]) )
                                reached = true;
                            uiController.loopMainThreadForAtLeast(500);
                        }
                    }
                }
                while(!reached && foundView);
            }
        };
    }


    /**
     * Custom matcher for comparing drawables of a view component, useful for debugging correct
     * tile digits being displayed
     * @param targetBackground the background to be matched against
     * @return A valid view matcher for check assertions
     */
    private static Matcher<View> checkTargetBackground(final Drawable targetBackground){
        return new BoundedMatcher<View, Button>(Button.class) {
            @Override
            protected boolean matchesSafely(Button item) {
                LayerDrawable convertedLayers = (LayerDrawable) item.getBackground();
                boolean matching = false;
                Bitmap targetBitmap = getBitmap(targetBackground);
                for(int layer = 0; layer < convertedLayers.getNumberOfLayers(); layer++){
                    if(convertedLayers.getDrawable(layer).getClass() == targetBackground.getClass()) {
                        Bitmap comparisonBitmap = getBitmap(convertedLayers.getDrawable(layer));
                        matching = comparisonBitmap.sameAs(targetBitmap);
                        if(matching)
                            return true;
                    }
                }
                return matching;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Should match specific drawable background")
                        .appendText(targetBackground.toString());
            }

            private Bitmap getBitmap(Drawable drawable) {
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                return bitmap;
            }
        };
    }
    /**
     * General test for solving, moving, displaying, switching intent to scoreboard.
     */
    @Test
    public void test1_setupBoardUnsolved() {
        try {
            GameSelection.IS_GUEST = false;
            testRule.getActivity().setCurrentAccount(new Account("123","123"));

            onView(withId(R.id.grid)).check(matches(isDisplayed())).check(matches(hasChildCount(16)));
            onData(withTagValue(is((Object) 1))).atPosition(0)
                    .check(matches(checkTargetBackground(activity.getDrawable(R.drawable.ic_1))));
            onData(withTagValue(is((Object) 2))).check(matches(not(checkTargetBackground(activity.getDrawable(R.drawable.ic_1)))));
            onData(instanceOf(Button.class)).inAdapterView(withId(R.id.grid)).atPosition(0).perform(click());
            Thread.sleep(300);
            onView(withText(R.string.mc_invalid_tap))
                    .inRoot(withDecorView(not(is(testRule.getActivity().getWindow().getDecorView()))))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.SaveButton)).perform(click());
            Thread.sleep(300);
            onView(withText(R.string.ga_manual_save))
                    .inRoot(withDecorView(not(is(testRule.getActivity().getWindow().getDecorView()))))
                    .check(matches(isDisplayed()));
            onData(instanceOf(Button.class)).inAdapterView(withId(R.id.grid)).atPosition(10).perform(click());
            Thread.sleep(250);
            onData(instanceOf(Button.class)).inAdapterView(withId(R.id.grid)).atPosition(6).perform(click());
            Thread.sleep(250);
            onData(instanceOf(Button.class)).inAdapterView(withId(R.id.grid)).atPosition(2).perform(click());
            Thread.sleep(250);
            ViewInteraction undoButton = onView(allOf(instanceOf(Button.class), withId(R.id.UndoButton)));
            undoButton.perform(click()).perform(click()).perform(click()).perform(click());
            Thread.sleep(500);
            onView(withText(R.string.ga_cannot_undo))
                    .inRoot(withDecorView(not(is(testRule.getActivity().getWindow().getDecorView()))))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.chronometer)).perform(waitUntil(R.id.chronometer, 15000, "10", "20"));
            Thread.sleep(250);
            onView(withText(R.string.ga_auto_saved))
                    .inRoot(withDecorView(not(is(testRule.getActivity().getWindow().getDecorView()))))
                    .check(matches(isDisplayed()));
            onData(instanceOf(Button.class)).inAdapterView(withId(R.id.grid)).atPosition(15).perform(click());
            onView(isRoot()).perform(delayFor(R.id.text_undos, 1000));
            intended(hasComponent(ScoreBoard.class.getName()));
            onData(allOf(instanceOf(String.class), startsWith("123"))).atPosition(0).check(matches(withText(startsWith("123:      9"))));
            onView(withId(R.id.switchscoreboardview)).perform(click());
            onView(withId(R.id.switchscoreboardview)).perform(click());
            onView(withId(R.id.button_new_game)).perform(click());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test2_guestSaveTest(){
        try {
            List<Account> accountsList = new ArrayList<>();
            accountsList.add(new Account("123", "123"));
            ObjectOutputStream outputStream;
            outputStream = new ObjectOutputStream(activity.openFileOutput(ACCOUNTS_FILENAME, MODE_PRIVATE));
            outputStream.writeObject(accountsList);
            outputStream.close();
            testRule.getActivity().setCurrentAccount(null);
            onView(withId(R.id.text_currentUserGame)).check(matches(withText("Guest")));
            onView(withId(R.id.SaveButton)).perform(click());
            onView(withText(R.string.ga_guest_save))
                    .inRoot(withDecorView(not(is(testRule.getActivity().getWindow().getDecorView()))))
                    .check(matches(isDisplayed()));
            testRule.getActivity().setCurrentAccount(new Account("123","123"));
            onView(withId(R.id.SaveButton)).perform(click());
            onView(withText(R.string.ga_manual_save))
                    .inRoot(withDecorView(not(is(testRule.getActivity().getWindow().getDecorView()))))
                    .check(matches(isDisplayed()));
            onData(instanceOf(Button.class)).inAdapterView(withId(R.id.grid)).atPosition(15).perform(click());
            Thread.sleep(500);
            intended(hasComponent(ScoreBoard.class.getName()));
            onData(allOf(instanceOf(String.class), startsWith("123"))).check(matches(withText(startsWith("123:      9"))));
            onView(withId(R.id.lastscore)).check(matches(withText(startsWith("9"))));
        } catch (IOException e){

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3_guestScoreTest(){
        try {
            testRule.getActivity().setCurrentAccount(null);
            GameSelection.IS_GUEST = true;
            onView(withId(R.id.text_currentUserGame)).check(matches(withText("Guest")));
            onData(instanceOf(Button.class)).inAdapterView(withId(R.id.grid)).atPosition(15).perform(click());
            Thread.sleep(500);
            intended(hasComponent(ScoreBoard.class.getName()));
            onData(allOf(instanceOf(String.class), startsWith("Guest"))).inAdapterView(withId(R.id.scoreboard_list))
                    .check(matches(withText(startsWith("Guest:      9"))));
            onView(withId(R.id.lastscore)).check(matches(withText(startsWith("9"))));
            onView(withId(R.id.switchscoreboardview)).check(matches(isDisplayed())).perform(click());
            onView(withText(R.string.gsb_no_score))
                    .inRoot(toastMatch())
                    .check(matches(isDisplayed()));
            onView(withId(R.id.button_new_game)).perform(click());
            intended(hasComponent(GameActivity.class.getName()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test4_allDirectionTileTest(){
        GameSelection.IS_GUEST = false;
        testRule.getActivity().setCurrentAccount(new Account("123","123"));
        onData(instanceOf(Button.class)).inAdapterView(withId(R.id.grid)).atPosition(14).perform(click());
        onData(instanceOf(Button.class)).inAdapterView(withId(R.id.grid)).atPosition(10).perform(click());
        onData(instanceOf(Button.class)).inAdapterView(withId(R.id.grid)).atPosition(11).perform(click());
        onData(instanceOf(Button.class)).inAdapterView(withId(R.id.grid)).atPosition(15).perform(click());
    }

    private static TypeSafeMatcher<Root> toastMatch(){
        return new TypeSafeMatcher<Root>() {
            @Override
            protected boolean matchesSafely(Root item) {
                int type = item.getWindowLayoutParams().get().type;
                if ((type == WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 5)) {
                    IBinder windowToken = item.getDecorView().getWindowToken();
                    IBinder appToken = item.getDecorView().getApplicationWindowToken();
                    if (windowToken == appToken) {
                        return true;
                    }
                }
                return false;
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("toast matching");
            }
        };
    }
}