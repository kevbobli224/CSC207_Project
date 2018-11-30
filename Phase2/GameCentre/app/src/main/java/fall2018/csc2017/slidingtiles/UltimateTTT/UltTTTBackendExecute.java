package fall2018.csc2017.slidingtiles.UltimateTTT;

//Adapted from: https://github.com/Prakash2403/UltimateTicTacToe/blob/master/app/src/main/java/com/example/prakash/ultimatetictactoe/backend/Backend.java

import org.json.JSONObject;

import java.util.Map;

import fall2018.csc2017.slidingtiles.UtilityManager;

/**
 * Class for Ultimate Tic Tac Toe backend execute.
 */
public class UltTTTBackendExecute {
    /**
     * The initializer for ultimate tic tac toe
     */
    public UltTTTBackendInit initializer;
    /**
     * The game state for ultimate tic tac toe
     */
    private UltTTTGameStates gamestates;
    /**
     * The scanner for ultimate tic tac toe
     */
    private UltTTTGameStateScanner scanner;

    /**
     * Initialize backend execute for ultimate tic tac toe
     *
     * @param initializer the initializer for ultimate tic tac toe
     * @param gamestates  the game state for ultimate tic tac toe
     */
    UltTTTBackendExecute(UltTTTBackendInit initializer, UltTTTGameStates gamestates) {
        this.initializer = initializer;
        this.gamestates = gamestates;
        scanner = gamestates.getScanner();
    }

    /**
     * Execute movements depends on button press
     *
     * @param cell_number the cell number
     * @return JSONObject the game state
     */
    public JSONObject execute(int cell_number) {
        int row, column;
        JSONObject currState;

        initializer.resetCells = new String();
        initializer.currentActiveBlock = getBlockNumber(cell_number);
        initializer.buttonPressed = getButtonPressed(cell_number);
        if (initializer.buttonPressed.equals("Reset")) {
            executeReset();
            initializer.buttonPressed = "Reset";
            currState = gamestates.toJson();
            gamestates.updateHistory(currState);
            return currState;
        } else if (initializer.buttonPressed.equals("Undo")) {
            executeUndo();
            initializer.buttonPressed = "Undo";
            return gamestates.toJson();
        }
        initializer.currentCell = cell_number;
        row = getRowNumber(cell_number);
        column = getColumnNumber(cell_number);

        initializer.usedCells.append(cell_number + "::::");
        gamestates.updateBoard(initializer.currentActiveBlock, row, column);
        initializer.no_terms[initializer.currentActiveBlock]++;

        initializer.winner = gamestates.getWinner(initializer.currentActiveBlock);

        switch (initializer.winner) {
            case "Player 1":
                initializer.disabledBlocks[initializer.currentActiveBlock] = true;
                gamestates.updateScore(0);
                break;
            case "Player 2":
                initializer.disabledBlocks[initializer.currentActiveBlock] = true;
                gamestates.updateScore(1);
                break;
        }
        initializer.nextActiveBlock = gamestates.getNextActiveBlock(cell_number);
        if (!initializer.winner.equals("None"))
            initializer.result.add(initializer.winner);

        initializer.globalWinner = scanner.findGlobalWinner();
        currState = gamestates.toJson();
        gamestates.updateHistory(currState);
        UtilityManager.saveUltTTTBoardManager(scanner.getActivity(), scanner.getActivity().getCurrentAccount(), cell_number);
        gamestates.updateTurn();
        return currState;
    }

    /**
     * Reset to initial state
     */
    public void executeReset() {
        initializer.initialize();
    }

    /**
     * Undo the execute
     */
    private void executeUndo() {
        Map previous_values;
        Map current_values;
        JSONObject previous_move;
        JSONObject current_move;
        String used_cells_raw_string_array[];
        String pre_nab;
        if (initializer.history.isEmpty()) {
            initializer.initialize();
            return;
        }
        current_move = initializer.history.pop();

        if (initializer.history.isEmpty()) {
            initializer.initialize();
            return;
        }

        initializer.isP1Turn = !initializer.isP1Turn;
        previous_move = initializer.history.peek();
        current_values = UltimateTTTInfoManager.parseJson(current_move);
        previous_values = UltimateTTTInfoManager.parseJson(previous_move);
        initializer.nextActiveBlock = Integer.parseInt((String) previous_values.get("NextActiveBlock"));
        initializer.currentActiveBlock = Integer.parseInt((String) previous_values.get("CurrentActiveBlock"));
        initializer.winner = (String) current_values.get("CurrentWinner");
        pre_nab = (String) current_values.get("CurrentActiveBlock");
        initializer.resetCells = (String) current_values.get("CurrentCell");
        initializer.resetBlockColor = (String) current_values.get("NextActiveBlock");
        used_cells_raw_string_array = initializer.usedCells.toString().split("::::");
        initializer.usedCells = new StringBuilder();
        for (int i = 0; i < used_cells_raw_string_array.length; i++)
            if (!used_cells_raw_string_array[i].equals(initializer.resetCells))
                initializer.usedCells.append(used_cells_raw_string_array[i] + "::::");
        initializer.score[0] = Integer.parseInt((String) previous_values.get("ScoreP1"));
        initializer.score[1] = Integer.parseInt((String) previous_values.get("ScoreP2"));

        // boardstatus is a 3D array.
        System.out.println("Print it" + pre_nab);
        initializer.boardStatus[Integer.parseInt(pre_nab)]
                [getRowNumber(Integer.parseInt(initializer.resetCells))]
                [getColumnNumber(Integer.parseInt(initializer.resetCells))] = -1;
        if (!initializer.winner.equals("None")) {
            initializer.disabledBlocks[Integer.parseInt(pre_nab)] = false;
            initializer.result.remove(0);
        }
    }

    /**
     * Gets the column number that current cell is in
     *
     * @param cell_number the cell number
     * @return the column number that current cell is in
     */
    private int getColumnNumber(int cell_number) {
        return (cell_number % 9) % 3;
    }

    /**
     * Gets the row number that current cell is in
     *
     * @param cell_number the cell number
     * @return the row number that current cell is in
     */
    private int getRowNumber(int cell_number) {
        return (cell_number % 9) / 3;
    }

    /**
     * Gets the block number that current cell is in
     *
     * @param cell_number the cell number
     * @return the block number that current cell is in
     */
    private int getBlockNumber(int cell_number) {
        return cell_number / 9;
    }

    /**
     * Gets the button pressed
     *
     * @param cell_number the cell number
     * @return the button pressed
     */
    private String getButtonPressed(int cell_number) {
        if (cell_number >= 0 && cell_number <= 80)
            return "GameButton";
        else if (cell_number == 100)
            return "Reset";
        else if (cell_number == 200)
            return "Undo";
        return "None";
    }
}
