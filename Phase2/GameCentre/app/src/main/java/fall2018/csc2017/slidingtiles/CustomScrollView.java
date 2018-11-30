package fall2018.csc2017.slidingtiles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.widget.ListView;

public class CustomScrollView extends ListView {
    /**
     * background for the scrollview
     */
    private final Drawable background =
            ResourcesCompat.getDrawable(getResources(), R.drawable.bg_simplebg, null);


    /**
     * Constructor for the scrollview given context used in
     * @param context the context of the scrollview
     * @param attrs attributes of the scrollview
     */
    public CustomScrollView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context);
    }

    /**
     * initializer for the background
     * @param context context of the scrollview
     */
    private void init(final Context context){
        background.setAlpha(100);
    }
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
    }
    @Override
    protected void onMeasure(int width, int height){
        setBackground(background);
        setMeasuredDimension(width, height);
    }

}
