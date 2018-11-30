package fall2018.csc2017.slidingtiles;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.Iterator;

public class ImageResultReceiver extends ResultReceiver {
    /**
     * The receiver that this receiver will relay information to.
     * Usually user has to set it after initialization.
     * Can be this receiver if functionality relies solely on relaying information in this receiver
     */
    private ImageResultReceiver mReceiver;
    /**
     * The image view component that this receiver will relay preview image to
     */
    private ImageView imageView;
    /**
     * The bitmap array which contains bitmaps in order
     */
    private Bitmap[][] bmArr;
    /**
     * The list of bitmaps that will be converted to from an array
     */
    private ArrayList<Bitmap> bmList;
    /**
     * The states of which whether this receiver has received
     * an image, image array, or invalid image url. Respectively.
     */
    private boolean recIm, recImArr, recImInvalid;
    /**
     * The rows and columns that the images will intend to have regardless
     * of changed dimensions after receiving an image
     */
    private int row = 4, col = 4;
    /**
     * Untouched, yet loaded bitmap awaiting for splitting
     */
    private Bitmap unprocessedBitmap;
    private boolean recInit = false;

    /**
     * The constructor for this ImageResultReceiver
     * @param handler the handler of this receiver
     * @param imageView the imageView that this receiver will have its image be previewed on
     */
    public ImageResultReceiver(Handler handler, ImageView imageView) {
        super(handler);
        this.imageView = imageView;
    }
    /**
     * Sets the receiver that this receiver will relay information to
     * @param r the receiver this receiver will relay information to, usually itself
     */
    public void setReceiver(ImageResultReceiver r){
        mReceiver = r;
    }
    /**
     * Converts the bitmap array to array list of bitmap for ease of iteration
     * @param bArr the bitmap Array that will be converted from
     * @return the converted array list of bitmaps
     */
    public ArrayList<Bitmap> getBitmapList(final Bitmap[][] bArr){
        Iterator<Bitmap> bmIt = new Iterator<Bitmap>() {
            private int jIndex = 0, kIndex = 0;
            private int jLength = bArr.length, kLength = bArr[0].length;
            @Override
            public boolean hasNext() {
                return (jLength + kLength - 1) > (jIndex + kIndex);
            }

            @Override
            public Bitmap next() {
                if(kIndex >= kLength)
                {
                    kIndex = 0;
                    jIndex++;
                }
                return bArr[jIndex][kIndex++];
            }
        };
        ArrayList<Bitmap> bList = new ArrayList<>();
        while (bmIt.hasNext()){
            bList.add(bmIt.next());
        }
        return bList;
    }

    /**
     * Get the unprocessed bitmap for processing purpose
     * @return a bitmap
     */
    public Bitmap getUnprocessedBitmap() {
        return unprocessedBitmap;
    }
    /**
     * Whether or not this receiver has received contents
     * @return boolean whether or not this receiver has received contents
     */
    public boolean contentReceived(){
        return (recIm || recImArr) ;
    }
    public boolean resultReceiverInitialized(){
        return recInit;
    };
    /**
     * Returns whether or not the sender has received invalid image png
     * @return the boolean of whether or not the sender has received invalid image png
     */
    public boolean invalidImageLink(){
        return recImInvalid;
    }

    /**
     * Handles the information sent by its sender
     * @param resultCode the code of the information from the sender
     * @param resultData the data from the sender
     */
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            switch(resultCode){
                case 1:
                    row = resultData.getIntegerArrayList("size").get(0);
                    col = resultData.getIntegerArrayList("size").get(0);
                    Bitmap bm = resultData.getParcelable("image");
                    imageView.setImageBitmap(bm);
                    recIm = true;
                    Bitmap[][] bArr = (Bitmap[][]) resultData.getSerializable("imagearray");
                    if(bArr != null) {
                        bmArr = bArr;
                        bmList = getBitmapList(bmArr);
                        recImArr = true;
                    }
                    recInit = true;
                    break;
                case 2:
                    recImInvalid = true;
                    recInit = true;
                    break;
                case 3:
                    Bitmap bm1 = resultData.getParcelable("image");
                    imageView.setImageBitmap(bm1);
                    unprocessedBitmap = bm1;
                    recIm = true;
                    recImArr = false;
                    recInit = true;
                    break;
                default:
                    recImArr = false;
                    recIm = false;
                    recInit = true;
                    break;
            }
        }
    }
}
