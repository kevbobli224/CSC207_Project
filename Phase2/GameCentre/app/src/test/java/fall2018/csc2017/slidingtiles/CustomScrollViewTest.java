package fall2018.csc2017.slidingtiles;

import android.content.Context;
import android.graphics.Canvas;
import android.support.test.runner.AndroidJUnit4;
import android.util.AttributeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

/**
 * Simple test for CustomScrollView, doesn't require to be test, yet here it is
 */
@RunWith(AndroidJUnit4.class)
public class CustomScrollViewTest {
    /**
     * The context this test will be tested in.
     * Provided by Robolectric's mocking android context framework.
     */
    private Context appContext;
    /**
     * Custom Scroll View instance
     */
    private CustomScrollView csv;
    /**
     * Very simple View model testing.
     */
    @Test
    public void easyTest(){
        appContext = RuntimeEnvironment.application;
        AttributeSet randomSet = Robolectric.buildAttributeSet().build();
        csv = new CustomScrollView(appContext, randomSet);
        csv.onDraw(new Canvas());
        csv.onMeasure(80, 80);
    }

}