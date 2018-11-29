package fall2018.csc2017.slidingtiles;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * modified intent service to handle splitting an image
 */
public class ImageServiceIntent extends IntentService {
    /**
     * The bundle that this intent will be sending to a receiver
     */
    private Bundle bundle;
    /**
     * The receiver that this intent will send information to
     */
    private ResultReceiver rr;
    /**
     * The URL to be handled as an image
     */
    private String url;
    /**
     * The dimensions of which images will be split proportionally
     */
    private int rows, columns;

    /**
     * The required constructors for assigning worker's name.
     */
    public ImageServiceIntent(String name) {
        super(name);
    }
    public ImageServiceIntent() {
        super("DisplayNotification");
    }
    /**
     * Initializes this intent service and creates an empty bundle
     */
    @Override
    public void onCreate(){
        super.onCreate();
        bundle = new Bundle();
    }

    /**
     * The implementation of the functionality this service intent will handle
     * @param intent the intent that is using this service intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        rr = intent.getParcelableExtra("receiver");
        url = intent.getStringExtra("url");
        rows = intent.getIntExtra("rows", 0);
        columns = intent.getIntExtra("columns", 0);
        try{
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "drawable");
            BitmapDrawable bitmap = (BitmapDrawable) d;
            if(bitmap == null){
                rr.send(2, null);
            }
            else if(rows == 0 && columns == 0) {
                bundle.putParcelable("image", bitmap.getBitmap());
                rr.send(3, bundle);
            } else {
                bundle.putParcelable("image", bitmap.getBitmap());
                bundle.putSerializable("imagearray", bitmapSplitter(bitmap.getBitmap(), rows, columns));
                bundle.putIntegerArrayList("size", new ArrayList<Integer>() {{
                    add(rows);
                    add(columns);
                }});
                intent.putExtra("splits", 16);
                rr.send(1, bundle);
            }
        } catch (MalformedURLException e) {
            rr.send(2,null);
            e.printStackTrace();
        } catch (IOException e) {
            rr.send(3,null);
            e.printStackTrace();
        }
    }

    /**
     * Processes the image to multiple separate images
     * @param bm the intent that is using this service intent
     * @param rows the rows which the board is set to have
     * @param columns the columns which the board is set to have
     * @return the bitmap array which stores its corresponding images in order
     */
    public static Bitmap[][] bitmapSplitter(Bitmap bm, int rows, int columns){
        if(rows == 0 || columns == 0)
            return null;
        Bitmap[][] bmArr = new Bitmap[rows][columns];
        int dWidth = bm.getWidth()/rows;
        int dHeight = bm.getHeight()/columns;
        for(int row = 0; row < rows; row++){
            for(int column = 0; column < columns; column++){
                bmArr[row][column] = Bitmap.createBitmap(bm, column*dWidth, row*dHeight, dWidth, dHeight);
            }
        }
        return bmArr;
    }
}
