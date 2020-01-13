
import java.util.ArrayList;

public class Board {

    private final double OPPORTUNITY_THREE_FACTOR = 1000.0; // Should have enough spread on weights to avoid block/win paradox
    private final double OPPORTUNITY_TWO_FACTOR = 100.0;
    private final double OPPORTUNITY_ONE_FACTOR = 10.0;

    private final double POWER_POSITION_FACTOR = 5.0;
    private final double POWER_OPPONENT_POSITION_FACTOR = 2.5;

    private final double BLOCKING_THREE_FACTOR = this.OPPORTUNITY_THREE_FACTOR / 5.0;
    private final double BLOCKING_TWO_FACTOR = this.OPPORTUNITY_TWO_FACTOR / 5.0;
    private final double BLOCKING_ONE_FACTOR = this.OPPORTUNITY_ONE_FACTOR / 5.0;

    private final double MULTILEVEL_FACTOR = 1.5; // Multilevel's become more important as the game progresses (higher value = faster rate of importance).
    private final double CORNER_FACTOR = 1.5; // Corners become less important as the game progresses (lower value = corners are valued less).

    public int[][][] board;
    public int turnCount;
    private static ArrayList<Goal> goalStates;

    public int totalFilled = 0;

    public Board() {
        this.board = new int[4][4][4];
        this.turnCount = 0;
        goalStates = this.generateGoalStates();
    }

    public Board(int[][][] tiles, int turnCount) {
        this.board = tiles;
        this.turnCount = turnCount;
    }

    public Board move(Coordinate move, int player) {
        Board newBoard = this.copy();
        newBoard.board[move.x][move.y][move.z] = player;
        return newBoard;
    }

    public boolean isSquareBlank(Coordinate square) {
        return this.board[square.x][square.y][square.z] == 0;
    }

    public ArrayList<Coordinate> getOpenSpots() {
        ArrayList<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                for (int k = 0; k < board[i].length; k++) {
                    if (this.board[i][j][k] == 0) {
                        coords.add(new Coordinate(i, j, k));
                    }
                }
            }
        }
        return coords;
    }

    public boolean isGoalState() {
        int player = -1;
        for (Goal g : goalStates) {
            boolean match = true;
            if (this.containsPlayer(g, 1)) {
                player = 1;
            } else if (this.containsPlayer(g, 2)) {
                player = 2;
            } else {
                continue; // Not a goal state if both players are in this line, or no players are in this line
            }
            for (Coordinate point : g.points) {
                // If this goalState doesn't allign, break out of the loop
                if (this.board[point.x][point.y][point.z] != player) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        return false;
    }

    private boolean containsPlayer(Goal goal, int player) { // Does this goal state contain this player?
        for (Coordinate point : goal.points) {
            if (this.getPlayerAt(point) == player) {
                return true;
            }
        }
        return false;
    }

    private int numPiecesInLine(Goal line, int player) {
        int count = 0;
        for (Coordinate point : line.points) {
            if (this.getPlayerAt(point) == player) {
                count++;
            }
        }
        return count;
    }

    public int blockingFactor(int currentPlayer, int opponent) {
        int count = 0;
        for (Goal goalState : goalStates) {
            int numOpponentPiecesInGoalState = this.numPiecesInLine(goalState, opponent);
            switch (numOpponentPiecesInGoalState) {
                case 1:
                    count += (TicTacToe.PRIORITIZE_CORNER_MOVES && goalState.hasCorners() ? this.CORNER_FACTOR : 1) * (TicTacToe.PRIORITIZE_MULTILEVEL_MOVES && goalState.isMultiLevel() ? this.MULTILEVEL_FACTOR : 1) * (numOpponentPiecesInGoalState * this.BLOCKING_ONE_FACTOR);
                    break;
                case 2:
                    count += (TicTacToe.PRIORITIZE_CORNER_MOVES && goalState.hasCorners() ? this.CORNER_FACTOR : 1) * (TicTacToe.PRIORITIZE_MULTILEVEL_MOVES && goalState.isMultiLevel() ? this.MULTILEVEL_FACTOR : 1) * (numOpponentPiecesInGoalState * this.BLOCKING_TWO_FACTOR);
                    break;
                case 3:
                    count += (TicTacToe.PRIORITIZE_CORNER_MOVES && goalState.hasCorners() ? this.CORNER_FACTOR : 1) * (TicTacToe.PRIORITIZE_MULTILEVEL_MOVES && goalState.isMultiLevel() ? this.MULTILEVEL_FACTOR : 1) * (numOpponentPiecesInGoalState * this.BLOCKING_THREE_FACTOR);
                    break;
            }
        }
        return count;
    }

    private int numPowerPositions(int player) {
        int count = 0;
        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[i].length; j++) {
                for (int k = 0; k < this.board[i][j].length; k++) {
                    Coordinate point = new Coordinate(i, j, k);
                    if (this.getPlayerAt(point) == player && (point.isCorner() || point.isMiddle())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public int evaluationFunction(int playerID) {
        int opponentID = getNextPlayer(playerID);
        int numMyThreeInARows = 0;
        int numOpponentThreeInARows = 0;
        int numMyPowerPositions = 0;
        int numOpponentPowerPositions = 0;
        int total = 0;
        int numMyPiecesInLine = 0;
        int numOpponentPiecesInLine = 0;

        if (TicTacToe.CONSIDER_POWER_POSITIONS) {
            numMyPowerPositions = this.numPowerPositions(playerID);
            numOpponentPowerPositions = this.numPowerPositions(opponentID);
        }

        for (Goal goalState : goalStates) {
            numMyPiecesInLine += this.numPiecesInLine(goalState, playerID);
            numOpponentThreeInARows += this.numPiecesInLine(goalState, opponentID);
            numMyThreeInARows = 0;

            if (numMyPiecesInLine != 0 && numOpponentPiecesInLine != 0) {
                numMyPiecesInLine = numOpponentPiecesInLine = 0;
            }
            int counter = numMyPiecesInLine != 0 ? numMyPiecesInLine : -numOpponentPiecesInLine;

            switch (Math.abs(counter)) {
                case 4:
                    if (counter > 0) {
                        return Integer.MAX_VALUE;
                    } else if (counter < 0) {
                        return Integer.MIN_VALUE;
                    }
                    break;
                case 3:
                    // 3 in this row
                    if (counter > 0) {
                        numMyThreeInARows++;
                    } else {
                        numOpponentThreeInARows++;
                    }
                    total += (TicTacToe.PRIORITIZE_CORNER_MOVES && goalState.hasCorners() ? (TicTacToe.COUNT_TURNS ? (this.CORNER_FACTOR * this.turnCount) : this.CORNER_FACTOR) : 1) * (TicTacToe.PRIORITIZE_MULTILEVEL_MOVES && goalState.isMultiLevel() ? (TicTacToe.COUNT_TURNS ? (this.turnCount * this.MULTILEVEL_FACTOR) : this.MULTILEVEL_FACTOR) : 1) * ((Math.signum(counter) * OPPORTUNITY_THREE_FACTOR));
                    break;
                case 2:
                    total += (TicTacToe.PRIORITIZE_CORNER_MOVES && goalState.hasCorners() ? (TicTacToe.COUNT_TURNS ? (this.CORNER_FACTOR * this.turnCount) : this.CORNER_FACTOR) : 1) * (TicTacToe.PRIORITIZE_MULTILEVEL_MOVES && goalState.isMultiLevel() ? (TicTacToe.COUNT_TURNS ? (this.turnCount * this.MULTILEVEL_FACTOR) : this.MULTILEVEL_FACTOR) : 1) * ((Math.signum(counter) * OPPORTUNITY_TWO_FACTOR));
                    break;
                case 1:
                    // 1 in this row
                    total += (TicTacToe.PRIORITIZE_CORNER_MOVES && goalState.hasCorners() ? (TicTacToe.COUNT_TURNS ? (this.CORNER_FACTOR * this.turnCount) : this.CORNER_FACTOR) : 1) * (TicTacToe.PRIORITIZE_MULTILEVEL_MOVES && goalState.isMultiLevel() ? (TicTacToe.COUNT_TURNS ? (this.turnCount * this.MULTILEVEL_FACTOR) : this.MULTILEVEL_FACTOR) : 1) * ((Math.signum(counter) * OPPORTUNITY_ONE_FACTOR));
                    break;
            }

        }

        if (numMyThreeInARows >= 2) {
            total += this.OPPORTUNITY_THREE_FACTOR;
        } else if (numOpponentThreeInARows >= 2) {
            total -= this.OPPORTUNITY_THREE_FACTOR;
        }

        this.totalFilled = total;

        //System.out.println((int) ((this.POWER_POSITION_FACTOR * numMyPowerPositions) - (this.POWER_OPPONENT_POSITION_FACTOR * numOpponentPowerPositions)) + total + (TicTacToe.TURBO_BLOCKING ? this.blockingFactor(playerID, opponentID) : 0));
        return (int) ((this.POWER_POSITION_FACTOR * numMyPowerPositions) - (this.POWER_OPPONENT_POSITION_FACTOR * numOpponentPowerPositions)) + total + (TicTacToe.TURBO_BLOCKING ? this.blockingFactor(playerID, opponentID) : 0);

    }

    public void print() {
        for (int i = 0; i < board.length; i++) {
            System.out.println("\n" + i + " ----");
            for (int j = 0; j < board[i].length; j++) {
                for (int k = 0; k < board[i][j].length; k++) {
                    System.out.print(this.board[i][j][k] + " ");
                }
                System.out.println("");
            }
        }
    }

    public int getPlayerAt(Coordinate coord) {
        return this.board[coord.x][coord.y][coord.z];
    }

    public Board copy() {
        int[][][] newBoard = new int[4][4][4];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                for (int k = 0; k < board[i][j].length; k++) {
                    newBoard[i][j][k] = board[i][j][k];
                }
            }
        }

        return new Board(newBoard, this.turnCount);
    }

    public static int getNextPlayer(int currentPlayer) {
        return (currentPlayer == 1) ? 2 : 1;
    }

    private ArrayList<Goal> generateGoalStates() {
        // Single plane: horizontal
        ArrayList<Goal> list = new ArrayList<>();
        for (int level = 0; level < this.board.length; level++) {
            for (int row = 0; row < this.board[0].length; row++) {
                Goal goal = new Goal();
                for (int column = 0; column < this.board[0][0].length; column++) {
                    Coordinate point = new Coordinate(column, row, level);
                    goal.set(column, point);
                }
                list.add(goal);
            }
        }

        // Single plane: vertical
        for (int level = 0; level < this.board.length; level++) {
            for (int row = 0; row < this.board[0].length; row++) {
                Goal goal = new Goal();
                for (int column = 0; column < this.board[0][0].length; column++) {
                    Coordinate point = new Coordinate(row, column, level);
                    goal.set(column, point);
                }
                list.add(goal);
            }
        }

        // Single plane: diagonals
        for (int level = 0; level < this.board.length; level++) {
            Goal goal1 = new Goal();
            Goal goal2 = new Goal();
            for (int i = 0; i < this.board[0].length; i++) {
                Coordinate point1 = new Coordinate(i, i, level);
                Coordinate point2 = new Coordinate(this.board[0][0].length - 1 - i, i, level);
                goal1.set(i, point1);
                goal2.set(i, point2);
            }
            list.add(goal1);
            list.add(goal2);
        }

        // Multi-plane: vertical columns
        for (int row = 0; row < this.board[0].length; row++) {
            for (int column = 0; column < this.board[0][0].length; column++) {
                Goal goal = new Goal();
                for (int level = 0; level < this.board.length; level++) {
                    Coordinate point = new Coordinate(column, row, level);
                    goal.set(level, point);
                }
                list.add(goal);
            }
        }

        // Multi-plane: side to side diagonal
        for (int row = 0; row < this.board[0].length; row++) {
            Goal goal1 = new Goal();
            Goal goal2 = new Goal();
            for (int i = 0; i < this.board.length; i++) {
                Coordinate point1 = new Coordinate(i, row, i);
                Coordinate point2 = new Coordinate(this.board[0][0].length - 1 - i, row, i);
                goal1.set(i, point1);
                goal2.set(i, point2);
            }
            list.add(goal1);
            list.add(goal2);
        }

        // Multi-plane: front to back diagonal
        for (int column = 0; column < this.board[0].length; column++) {
            Goal goal1 = new Goal();
            Goal goal2 = new Goal();
            for (int i = 0; i < this.board.length; i++) {
                Coordinate point1 = new Coordinate(column, i, i);
                Coordinate point2 = new Coordinate(column, this.board[0][0].length - 1 - i, i);
                goal1.set(i, point1);
                goal2.set(i, point2);
            }
            list.add(goal1);
            list.add(goal2);
        }

        // Multi-plane: corner to corner diagonal
        Goal goal1 = new Goal();
        Goal goal2 = new Goal();
        Goal goal3 = new Goal();
        Goal goal4 = new Goal();
        for (int i = 0; i < this.board.length; i++) {
            goal1.set(i, new Coordinate(i, i, i));
            goal2.set(i, new Coordinate(this.board[0][0].length - 1 - i, i, i));
            goal3.set(i, new Coordinate(this.board[0][0].length - 1 - i, this.board[0][0].length - 1 - i, i));
            goal4.set(i, new Coordinate(i, this.board[0][0].length - 1 - i, i));
        }
        list.add(goal1);
        list.add(goal2);
        list.add(goal3);
        list.add(goal4);

        return list;
    }

    public ArrayList<Goal> getGoalStates() {
        return goalStates;
    }

}
