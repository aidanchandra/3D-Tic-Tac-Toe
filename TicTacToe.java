
import java.util.Scanner;
import java.util.Stack;

public class TicTacToe {

    private static final int MAX_PLY_DEPTH = 2;
    private static final boolean ENABLE_GUI = true;
    public static final boolean DEBUG = true;
    public static final boolean PRIORITIZE_CORNER_MOVES = true;
    public static final boolean PRIORITIZE_MULTILEVEL_MOVES = true;
    public static final boolean TURBO_BLOCKING = true;
    public static final boolean COUNT_TURNS = true;
    public static final boolean ENABLE_AB_PRUNING = true;
    public static final boolean ENABLE_MOVE_ORDERING = false;
    public static final boolean ENABLE_AI_TIMER = false;
    public static final boolean CONSIDER_POWER_POSITIONS = true;
    private static final boolean ENABLE_TEST_UTILITY = false;
    
    static SerialManager manager;
    private static final boolean PHYSICAL_BOARD = false && !ENABLE_GUI;

    private static GUI gui;
    public static Board board;
    public static boolean isAITurn = true;

    public static void main(String[] args) throws InterruptedException{
        if (ENABLE_TEST_UTILITY) {
            // TESTER FOR SETTING UP BOARD CONFIGS. HAS UNDO CAPABILITY
            Scanner scanner = new Scanner(System.in);
            Board board = new Board();
            Coordinate move = null;
            Stack<Coordinate> lastMoves = new Stack<>();
            while (true) {
                System.out.print("\nPlayer | ");
                move = getMove(board, scanner, lastMoves);
                board = board.move(move, 1);
                lastMoves.add(move);
                board.print();
                System.out.println("Eval for Player: " + board.evaluationFunction(1));
                System.out.println("Eval for AI: " + board.evaluationFunction(2));
                System.out.print("\nAI | ");
                move = getMove(board, scanner, lastMoves);
                board = board.move(move, 2);
                lastMoves.add(move);
                board.print();
                System.out.println("Eval for Player: " + board.evaluationFunction(1));
                System.out.println("Eval for AI: " + board.evaluationFunction(2));
            }
        } else {
            Scanner scanner;
            if (ENABLE_GUI) {
                gui = new GUI(4);
            } else {
                scanner = new Scanner(System.in);
                manager = new SerialManager();
                manager.resetBoard();
                System.out.println("Welcome to 3D Tic Tac Toe.");
                System.out.println("Player: input coordinates as `level <space> row <space> column` where (0,0,0) is top left corner of bottom level");
            }
            board = new Board();

            while (board.getOpenSpots().size() > 0 && !board.isGoalState()) {
                if (!ENABLE_GUI) {
                    // Player's turn
                    board.turnCount++;
                    board = playerMove(board, scanner, 1);
                    if (DEBUG) {
                        printDebugMsgs(1);
                    }
                    if (board.isGoalState()) {
                        System.out.println("Player won!");
                        break;
                    }
                    // AI's turn
                    board = AIMove(board, 2);
                    board.turnCount++;
                    if (DEBUG) {
                        printDebugMsgs(2);
                    }
                    if (board.isGoalState()) {
                        System.out.println("AI won!");
                        break;
                    }
                } else { // Using the GUI
                    if (isAITurn) {
                        board = AIMove(board, 2);
                        isAITurn = false;
                        board.turnCount++;
                        if (DEBUG) {
                            printDebugMsgs(2);
                        }
                        if (board.isGoalState()) {
                            gui.showWinMessage("AI won!");
                            isAITurn = true;
                            break;
                        }
                    }
                }
                if (board.getOpenSpots().isEmpty()) {
                    System.out.println("Tie: No spots left");
                    break;
                }
            }
            if (board.isGoalState() && !isAITurn) {
                gui.showWinMessage("Player won!");
            }
        }
    }

    public static Board AIMove(Board board, int aiTile) throws InterruptedException{
        AI ai = new AI();
        Coordinate move = ai.nextMove(board, aiTile, MAX_PLY_DEPTH).getMove();
        if (!board.isSquareBlank(move)) {
            System.out.println("Square not blank");
            return AIMove(board, aiTile);
        }
        board = board.move(move, aiTile);
        if (ENABLE_GUI) {
            gui.move(move);
        } else {
            board.print();
            System.out.println("AI moved " + move);
            if(PHYSICAL_BOARD){
                manager.move(move, 1);
            }
        }
        return board;

    }

    public static void printDebugMsgs(int currentPlayer) {
        board.print();
        System.out.println("Evaluation of that move: " + board.evaluationFunction(currentPlayer));
        if (TURBO_BLOCKING) {
            System.out.println("Blocking factor: " + board.blockingFactor(currentPlayer, Board.getNextPlayer(currentPlayer)));
        }
        //System.out.println("Total filled: " + board.totalFilled);
        if (COUNT_TURNS) {
            System.out.println("Turn count: " + board.turnCount);
        }
    }

    // Used only for the board configuration tester
    private static Coordinate getMove(Board board, Scanner scanner, Stack<Coordinate> moves) throws InterruptedException{
        if(!PHYSICAL_BOARD){
            System.out.print("Move: ");
            String input = "";
            int x = 0, y = 0, z = 0;

            if (scanner.hasNextLine()) {
                input = scanner.nextLine();
                if (input.equals("undo")) {
                    Coordinate lastMove = moves.pop();
                    board.board[lastMove.x][lastMove.y][lastMove.z] = 0;
                    board.print();
                    System.out.println("Eval for Player: " + board.evaluationFunction(1));
                    System.out.println("Eval for AI: " + board.evaluationFunction(2));
                    getMove(board, scanner, moves);
                }
                String[] parsed = input.split(" ");
                try {
                    x = Integer.parseInt(parsed[0]);
                    y = Integer.parseInt(parsed[1]);
                    z = Integer.parseInt(parsed[2]);
                    if ((x > 3 || x < 0) || (y > 3 || y < 0) || (z > 3 || z < 0)) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    System.out.println("Invalid move.");
                }

            } else {
                System.err.print("Invalid input");
            }

            return new Coordinate(x, y, z);
        }
        else{
            return manager.waitForMove(0, 100);
        }
    }

    public static Board playerMove(Board board, Scanner scanner, int playerTile) throws InterruptedException{
        System.out.print("Move: ");
        String input = "";
        int x = 0;
        int y = 0;
        int z = 0;
        if(!PHYSICAL_BOARD){
            if (scanner.hasNextLine()) {
                input = scanner.nextLine();
                String[] parsed = input.split(" ");
                try {
                    x = Integer.parseInt(parsed[0]);
                    y = Integer.parseInt(parsed[1]);
                    z = Integer.parseInt(parsed[2]);
                    if ((x > 3 || x < 0) || (y > 3 || y < 0) || (z > 3 || z < 0)) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    System.out.println("Invalid move.");
                    return playerMove(board, scanner, playerTile);
                }

            } else {
                System.out.println("Invalid input");
                return playerMove(board, scanner, playerTile);
            }
        }
        else{
            System.out.println("Waiting for move...");
            Coordinate cord = manager.waitForMove(0,100);
            x = cord.x;
            y = cord.y;
            z = cord.z;
        }
        Coordinate move;
        boolean success = false;
        do {
            move = new Coordinate(x, y, z);

            success = board.isSquareBlank(move);
            if (!success) {
                System.out.println("Invalid move. Try again.");
                return playerMove(board, scanner, playerTile);
            }
        } while (!success);
        board = board.move(move, playerTile);
        board.print();
        System.out.println(playerTile + " moved " + move);
        return board;
    }
}
