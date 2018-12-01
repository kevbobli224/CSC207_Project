package fall2018.csc2017.slidingtiles;

import java.util.ArrayList;
import java.util.List;

public class BoardSetupInstrumented {
    /**
     * Setup a nearly solved 4x4 board swapped by 1 move
     * @return BoardManager with nearly solved board
     */
    public static BoardManager setUp4x4NearSolved(){
        BoardManager bm = setUp4x4Solved();
        bm.touchMove(14);
        return bm;
    }
    /**
     * Setup a solved 4x4 board
     * @return BoardManager with solved board
     */
    public static BoardManager setUp4x4Solved(){
        return new BoardManager(new Board(setupCustomBoard(4,4)));
    }
    /**
     * Setup a solved n by n board
     * @return BoardManager with n by n solved board
     */
    private static List<Tile> setupCustomBoard(int row, int column){
        List<Tile> tiles = new ArrayList<>();
        int numTiles = row * column;
        for (int tileNum = 0; tileNum != numTiles; tileNum++) {
            tiles.add(new Tile(tileNum));
        }
        return tiles;
    }
}
