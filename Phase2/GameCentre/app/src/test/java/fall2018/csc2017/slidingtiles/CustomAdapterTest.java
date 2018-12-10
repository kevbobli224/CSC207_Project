package fall2018.csc2017.slidingtiles;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 *
 */
@RunWith(AndroidJUnit4.class)
public class CustomAdapterTest {
    /**
     * An android-framework adapted Context
     */
    private Context appContext;
    /**
     * List of Buttons to be initialized for the adapter
     */
    private List<Button> buttonList;
    /**
     * Initial reference of a new button
     */
    private Button initialButton;

    /**
     * Sets up the context by calling Robolectric's based context framework
     * Initializes the button list with initial button created
     */
    @Before
    public void setup(){
        appContext = RuntimeEnvironment.application;
        buttonList = new ArrayList<>();
        initialButton = new Button(RuntimeEnvironment.application);
        buttonList.add(initialButton);
    }

    /**
     * Tests getCount() by asserting number of buttons in a list and it's new adapter
     */
    @Test
    public void getCount() {
        CustomAdapter adapter = new CustomAdapter((ArrayList<Button>) buttonList, 5,5);
        assertEquals(1, adapter.getCount());
        buttonList.add(new Button(appContext));
        adapter = new CustomAdapter((ArrayList<Button>)buttonList, 5,5);
        assertEquals(2, adapter.getCount());
        buttonList.clear();
        adapter = new CustomAdapter((ArrayList<Button>)buttonList, 5,5);
        assertEquals(0, adapter.getCount());
    }

    /**
     * Tests for getItem() where 2 Buttons are compared by reference
     */
    @Test
    public void getItem() {
        Button newButton = new Button(appContext);
        buttonList.add(newButton);
        CustomAdapter adapter = new CustomAdapter((ArrayList<Button>) buttonList, 5,5);
        assertEquals(newButton, adapter.getItem(1));
        assertNotEquals(newButton, adapter.getItem(0));
    }

    /**
     * Tests for getItemId() where it supposed to return a View component which would be
     * auto-boxed for it's reference and compared with assertion
     */
    @Test
    public void getItemId() {
        Button newButton = new Button(appContext);
        buttonList.add(newButton);
        CustomAdapter adapter = new CustomAdapter((ArrayList<Button>) buttonList, 5,5);
        assertEquals(buttonList.indexOf(newButton), adapter.getItemId(1));
        assertNotEquals(buttonList.indexOf(newButton), adapter.getItemId(0));
    }

    /**
     * Gets the view where the second parameter specifies whether or not to use the specified view
     * for the return view
     */
    @Test
    public void getView() {
        Button newButton = new Button(appContext);
        buttonList.add(newButton);
        CustomAdapter adapter = new CustomAdapter((ArrayList<Button>) buttonList, 5,5);
        assertEquals(newButton, adapter.getView(1, null, null ));
        assertEquals(newButton, adapter.getView(1, newButton, null));
        assertNotEquals(newButton, adapter.getView(0, null, null ));
        assertNotEquals(newButton, adapter.getView(0, initialButton, null));
    }
}