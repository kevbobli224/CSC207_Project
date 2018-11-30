package fall2018.csc2017.slidingtiles;


import java.util.Observable;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * The sliding tiles board.
 */
public class Board extends Observable implements Serializable, Iterable<Tile> {

    /**
     * The number of rows.
     */
    private int numRows;

    /**
     * The number of columns.
     */
    private int numColumns;

    /**
     * The tiles on the board in row-major order.
     */
    private Tile[][] tiles;

    /**
     * A new board of tiles in row-major order.
     * Precondition: len(tiles) == NUM_ROWS * NUM_COLS
     *
     * @param tiles the tiles for the board
     */
    public Board(List<Tile> tiles) {
        setNumRows(4);
        setNumColumns(4);
        this.tiles = new Tile[4][4];
        Iterator<Tile> iter = tiles.iterator();
        for (int row = 0; row != 4; row++) {
            for (int col = 0; col != 4; col++) {
                this.tiles[row][col] = iter.next();
            }
        }
    }
    /**
     * A new board of tiles in row-major order.
     * Precondition: len(tiles) == NUM_ROWS * NUM_COLS
     *
     * @param tiles the tiles for the board
     * @param rows the number of rows of the board
     * @param columns the number of columns of the board
     */
    public Board(List<Tile> tiles, int rows, int columns){
        setNumRows(rows);
        setNumColumns(columns);
        this.tiles = new Tile[rows][columns];
        Iterator<Tile> iter = tiles.iterator();
        for (int row = 0; row != rows; row++) {
            for (int col = 0; col != columns; col++) {
                this.tiles[row][col] = iter.next();
            }
        }
    }

    /**
     * Return the number of tiles on the board.
     * @return the number of tiles on the board
     */
    int numTiles() {
        return tiles.length * tiles[0].length;
    }

    /**
     * Return the tile at (row, col)
     *
     * @param row the tile row
     * @param col the tile column
     * @return the tile at (row, col)
     */
    Tile getTile(int row, int col) {
        return tiles[row][col];
    }

    /**
     * Swap the tiles at (row1, col1) and (row2, col2)
     *
     * @param row1 the first tile row
     * @param col1 the first tile col
     * @param row2 the second tile row
     * @param col2 the second tile col
     */
    void swapTiles(int row1, int col1, int row2, int col2) {
        Tile firstTile = getTile(row1, col1);
        Tile secondTile = getTile(row2, col2);
        tiles[row1][col1] = secondTile;
        tiles[row2][col2] = firstTile;
        setChanged();
        notifyObservers();
    }

    /**
     * toString method that returns tiles of this board in row-major order
     * @return String of tile orders
     */
    @Override
    public String toString() {
        String tileOrder = "";
        Iterator tileIterator = this.iterator();
        while(tileIterator.hasNext()){
            tileOrder += tileIterator.next();
            if(tileIterator.hasNext())
                tileOrder += ", ";
        }
        return "Board{" +
                "tiles=" + tileOrder +
                '}';
    }

    /**
     * Iterator method that allows iteration through tiles in row-major order
     * @return A tile iterator
     */
    @Override
    public Iterator<Tile> iterator(){
        return new Iterator<Tile>() {
            private int jIndex = 0, kIndex = 0;
            private int jLength = tiles.length, kLength = tiles[0].length;
            @Override
            public boolean hasNext() {
                return (jLength + kLength - 1) > (jIndex + kIndex);
            }

            @Override
            public Tile next() {
                if(kIndex >= kLength)
                {
                    kIndex = 0;
                    jIndex++;
                }
                return tiles[jIndex][kIndex++];
            }
        };
    }

    /**
     * Return the dimensions of tiles on the board.
     * @return the dimensions of tiles on the board as String
     */
    public String getTilesDimension(){
        String returnString = tiles.length + "x" + tiles[0].length;
        return returnString;
    }

    /**
     * Get the number of rows.
     */
    public int getNumRows() {
        return numRows;
    }

    /**
     * Set the number of rows.
     */
    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    /**
     * Get the number of columns.
     */
    public int getNumColumns() {
        return numColumns;
    }

    /**
     * Set the number of columns.
     */
    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

}
