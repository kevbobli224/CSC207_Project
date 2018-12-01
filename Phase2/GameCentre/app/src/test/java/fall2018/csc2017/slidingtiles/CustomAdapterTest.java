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

@RunWith(AndroidJUnit4.class)
public class CustomAdapterTest {
    private Context appContext;
    private List<Button> buttonList;
    private Button initialButton;

    @Before
    public void setup(){
        appContext = RuntimeEnvironment.application;
        buttonList = new ArrayList<>();
        initialButton = new Button(RuntimeEnvironment.application);
        buttonList.add(initialButton);
    }
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

    @Test
    public void getItem() {
        Button newButton = new Button(appContext);
        buttonList.add(newButton);
        CustomAdapter adapter = new CustomAdapter((ArrayList<Button>) buttonList, 5,5);
        assertEquals(newButton, adapter.getItem(1));
        assertNotEquals(newButton, adapter.getItem(0));
    }

    @Test
    public void getItemId() {
        Button newButton = new Button(appContext);
        buttonList.add(newButton);
        CustomAdapter adapter = new CustomAdapter((ArrayList<Button>) buttonList, 5,5);
        assertEquals(buttonList.indexOf(newButton), adapter.getItemId(1));
        assertNotEquals(buttonList.indexOf(newButton), adapter.getItemId(0));
    }

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