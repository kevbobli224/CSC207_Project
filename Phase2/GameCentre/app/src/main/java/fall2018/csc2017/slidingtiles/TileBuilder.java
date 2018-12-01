package fall2018.csc2017.slidingtiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.Nullable;
import android.widget.Button;

import java.util.ArrayList;

public class TileBuilder implements Tileable{
    /**
     * Current BoardManager
     */
    private final BoardManager boardManager;
    /**
     * How many rows/columns as well as the width of a single tile
     */
    private final int rows, columns, columnWidth;
    /**
     * Array list of buttons to be created with.
     */
    private final ArrayList<Button> tileButtons;
    /**
     * The current context this builder is in
     */
    private Context currentContext;
    /**
     * Whether or not tiles will be using images
     */
    private boolean useImages;
    /**
     * The set of images the tiles will be using if using images
     */
    private ArrayList<Bitmap> imageSet;

    /**
     * Account constructor
     * @param boardManager the current boardManager to be building with
     * @param currentContext the current context
     * @param columnWidth the width of which tiles will be adjusted to be built on
     */
    public TileBuilder(BoardManager boardManager, Context currentContext, int columnWidth){
        this.boardManager = boardManager;
        this.tileButtons = new ArrayList<>();
        this.currentContext = currentContext;
        this.rows = boardManager.getBoard().getNumRows();
        this.columns = boardManager.getBoard().getNumColumns();
        this.columnWidth = columnWidth;
    }

    /**
     * Sets whether or not to use images and set the image collections
     * @param useImages whether or not images will be used
     * @param imageSet the list of split images
     */
    public void setUseImages(boolean useImages, ArrayList<Bitmap> imageSet){
        this.useImages = useImages;
        this.imageSet = imageSet;
    }

    /**
     * returns the list of buttons
     * @return List of buttons regardless of it's state
     */
    public ArrayList<Button> getTileButtons(){
        return tileButtons;
    }

    /**
     * Generates a normal tile's background
     * @param isBlank whether or not the tile should be blank
     * @param tile the tile to be generated.
     * @return A drawable where buttons can set their background to
     */
    @Override
    public Drawable generateBackground(boolean isBlank, Tile tile) {
        int tileId = tile.getId();
        if(isBlank)
        {
            if(!tile.hasBackground())
                tile.setBackground(currentContext.getDrawable(R.drawable.bg_simplebg));
            return currentContext.getDrawable(R.drawable.bg_simplebg);
        }
        else if(tileId < 10){
            LayerDrawable ld = generateTileLayers(DigitEnum.DIGIT_ONES, tileId);
            if(!tile.hasBackground())
                tile.setBackground(ld);
            return ld;
        }
        else if (tileId < 100){
            LayerDrawable ld = generateTileLayers(DigitEnum.DIGIT_TENS, tileId);
            if(!tile.hasBackground())
                tile.setBackground(ld);
            return ld;
        } else if (tileId < 1000)
        {
            LayerDrawable ld = generateTileLayers(DigitEnum.DIGIT_THOU, tileId);
            if(!tile.hasBackground())
                tile.setBackground(ld);
            return ld;
        }
        return null;
    }

    /**
     * Generates a image tile's background
     * @param isBlank whether or not the tile should be blank
     * @param tile the tile to be generated.
     * @param imageBitmap the image to set the tile with
     * @return A drawable where buttons can set their background to
     */
    @Override
    public Drawable generateImageBackground(boolean isBlank, Tile tile, @Nullable Bitmap imageBitmap) {
        if(isBlank)
        {
            if(!tile.hasBackground())
                tile.setBackground(currentContext.getDrawable(R.drawable.bg_simplebg));
            return currentContext.getDrawable(R.drawable.bg_simplebg);
        } else {
            Drawable bg = new BitmapDrawable(currentContext.getResources(), imageBitmap);
            if (!tile.hasBackground())
                tile.setBackground(bg);
            return bg;
        }
    }

    /**
     * Generates layers of drawables including background and digits
     * @param digitEnum enum for digits to generate
     * @param tileId the tile number to be genrated.
     * @return A LayerDrawable where buttons can set their background to
     */
    @Override
    public LayerDrawable generateTileLayers(DigitEnum digitEnum, int tileId) {
        Drawable bg = currentContext.getDrawable(R.drawable.bg_simplebg);
        String thouString, tensString, onesString;
        LayerDrawable ld;
        int onesNumberPath, tensNumberPath, thouNumberPath;
        switch (digitEnum) {
            case DIGIT_ONES:
                onesNumberPath = currentContext.getResources()
                        .getIdentifier("ic_"+Integer.toString(tileId),
                                "drawable", currentContext.getPackageName());
                return alignTilesDigits(new LayerDrawable(new Drawable[]
                                {bg,currentContext.getDrawable(onesNumberPath)}),
                        digitEnum);
            case DIGIT_TENS:
                tensString = Integer.toString(tileId).substring(0,1);
                onesString = Integer.toString(tileId).substring(1);
                onesNumberPath = currentContext.getResources()
                        .getIdentifier("ic_"+onesString, "drawable",
                                currentContext.getPackageName());
                tensNumberPath = currentContext.getResources()
                        .getIdentifier("ic_"+tensString, "drawable",
                                currentContext.getPackageName());
                return alignTilesDigits(new LayerDrawable(new Drawable[]
                                {bg,currentContext.getDrawable(tensNumberPath),
                                        currentContext.getDrawable(onesNumberPath)}),
                        digitEnum);
            case DIGIT_THOU:
                thouString = Integer.toString(tileId).substring(0,1);
                tensString = Integer.toString(tileId).substring(1,2);
                onesString = Integer.toString(tileId).substring(2);
                thouNumberPath = currentContext.getResources()
                        .getIdentifier("ic_"+thouString, "drawable",
                                currentContext.getPackageName());
                tensNumberPath = currentContext.getResources()
                        .getIdentifier("ic_"+tensString, "drawable",
                                currentContext.getPackageName());
                onesNumberPath = currentContext.getResources()
                        .getIdentifier("ic_"+onesString, "drawable",
                                currentContext.getPackageName());
                return alignTilesDigits(new LayerDrawable(new Drawable[]
                                {bg,currentContext.getDrawable(thouNumberPath),
                                        currentContext.getDrawable(tensNumberPath),
                                        currentContext.getDrawable(onesNumberPath)}),
                        digitEnum);
        }
        return null;
    }

    /**
     * Aligns digits in their correct position
     * @param generatedLayerDrawable the compiled drawables
     * @param digitEnum enum for digits to generate
     * @return A LayerDrawable where buttons can set their background to
     */
    @Override
    public LayerDrawable alignTilesDigits(LayerDrawable generatedLayerDrawable, DigitEnum digitEnum) {
        if( digitEnum == DigitEnum.DIGIT_ONES) {
            generatedLayerDrawable.setLayerInset(1, columnWidth / 4, 0, 0, 30);
            generatedLayerDrawable.setLayerWidth(1, columnWidth / 2);
            return generatedLayerDrawable;
        }
        for(int i = 1; i < generatedLayerDrawable.getNumberOfLayers(); i++)
        {
            if(digitEnum == DigitEnum.DIGIT_TENS) {
                generatedLayerDrawable.setLayerWidth(i, columnWidth / 2 - 5);
                generatedLayerDrawable.setLayerInset(i, i == 1 ? 0 : columnWidth/2, 0, 0, 30);
            }
            else if (digitEnum == DigitEnum.DIGIT_THOU){
                generatedLayerDrawable.setLayerWidth(i, columnWidth / 3 - 5);
                generatedLayerDrawable.setLayerInset(i, i == 1 ? 0 : (i-1)*columnWidth/3, 0, 0, 30);
            }
        }
        return generatedLayerDrawable;
    }

    /**
     * Builds the tile buttons
     */
    @Override
    public void createTileButtons() {
        Board board = boardManager.getBoard();
        ArrayList<Bitmap> imageSet = useImages ? this.imageSet: null;
        for (int row = 0; row != rows; row++) {
            for (int col = 0; col != columns; col++) {
                Button tmp = new Button(currentContext);
                Tile tile = board.getTile(row,col);
                int tileId = board.getTile(row,col).getId();
                tmp.setTag(tileId);
                if(imageSet != null){
                    if(board.getTile(row,col).getId() == columns * rows)
                        tmp.setBackground(generateImageBackground(true, tile, null));
                    tmp.setBackground(generateImageBackground(false, tile, imageSet.get(tileId-1)));
                }
                else if(board.getTile(row,col).getId() < columns * rows)
                    tmp.setBackground(generateBackground(false, tile));
                else
                    tmp.setBackground(generateBackground(true, tile));
                this.tileButtons.add(tmp);
            }
        }
    }
}
