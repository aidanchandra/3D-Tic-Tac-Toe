
import java.util.ArrayList;

public class AI {

    public static boolean timeDebug = TicTacToe.ENABLE_AI_TIMER && TicTacToe.ENABLE_MOVE_ORDERING; // For computation time metrics.
    private int orderTimeSum = 0;
    private int orderTimeCount = 0;

    public UtilMove nextMove(Board board, int player, int maxDepth) {
        orderTimeSum = 0;
        orderTimeCount = 0;

        long startTime = 0;
        if (timeDebug) {
            startTime = System.currentTimeMillis();
        }
        UtilMove bestMove = new UtilMove(Integer.MIN_VALUE, null);
        ArrayList<Coordinate> moves = board.getOpenSpots();
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        for (Coordinate move : moves) {
            Board temp = board.move(move, player);
            int score = min(Board.getNextPlayer(player), temp, maxDepth, alpha, beta);

            if (score >= bestMove.getUtility()) {
                bestMove.utility = score;
                bestMove.move = move;
            }
        }
        if (timeDebug) {
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println("Total time for move decision: " + (elapsedTime / 1000) + " seconds");
            System.out.println("Total time on move ordering: " + (orderTimeSum) + " milliseconds (" + (orderTimeSum / 1000) + " seconds)");
            System.out.println("Average time on move ordering: " + (orderTimeSum / (orderTimeCount)) + " milliseconds per iteration");
        }
        return bestMove;
    }

    private int min(int player, Board board, int depth, int alpha, int beta) {
        if (board.isGoalState() || depth <= 0) {
            return board.evaluationFunction(Board.getNextPlayer(player));
        }

        ArrayList<Board> validBoards = new ArrayList<>();
        for (Coordinate move : board.getOpenSpots()) {
            if (board.isSquareBlank(move)) {
                validBoards.add(board.move(move, player));
            }
        }

        if (TicTacToe.ENABLE_MOVE_ORDERING) {
            validBoards = orderMoves(validBoards, player);
        }

        int lowestMax = Integer.MAX_VALUE;
        for (Board b : validBoards) {
            int maxVal = this.max(Board.getNextPlayer(player), b, depth - 1, alpha, beta);
            if (TicTacToe.ENABLE_AB_PRUNING) {
                if (lowestMax <= alpha) {
                    return lowestMax;
                }
            }
            if (maxVal < lowestMax) {
                lowestMax = maxVal;
            }

            beta = Math.min(beta, lowestMax);
        }

        return lowestMax;
    }

    private int max(int player, Board board, int depth, int alpha, int beta) {
        if (board.isGoalState() || depth <= 0) {
            return board.evaluationFunction(Board.getNextPlayer(player));
        }

        ArrayList<Board> validBoards = new ArrayList<>();
        for (Coordinate move : board.getOpenSpots()) {
            if (board.isSquareBlank(move)) {
                validBoards.add(board.move(move, player));
            }
        }

        if (TicTacToe.ENABLE_MOVE_ORDERING) {
            validBoards = orderMoves(validBoards, player);
        }

        int largestMin = Integer.MIN_VALUE;
        for (Board b : validBoards) {
            int minVal = this.min(Board.getNextPlayer(player), b, depth - 1, alpha, beta);
            if (TicTacToe.ENABLE_AB_PRUNING) {
                if (largestMin >= beta) {
                    return largestMin;
                }
            }
            if (minVal > largestMin) {
                largestMin = minVal;
            }

            alpha = Math.max(alpha, largestMin);

        }

        return largestMin;
    }

    private ArrayList<Board> orderMoves(ArrayList<Board> boards, int player) {
        boards.sort((Board first, Board second) -> {
            if (first.evaluationFunction(player) < second.evaluationFunction(player)) {
                return 1;
            } else if (first.evaluationFunction(player) > second.evaluationFunction(player)) {
                return -1;
            } else {
                return 0;
            }
        });
        return boards;
    }

    private class SortingAlgorithms {

        public void quickSort(Board arr[], int begin, int end, int player) {
            if (begin < end) {
                int partitionIndex = partition(arr, begin, end, player);

                quickSort(arr, begin, partitionIndex - 1, player);
                quickSort(arr, partitionIndex + 1, end, player);
            }
        }

        private int partition(Board arr[], int begin, int end, int player) {
            Board pivot = arr[end];
            int i = (begin - 1);

            for (int j = begin; j < end; j++) {
                if (arr[j].evaluationFunction(player) <= pivot.evaluationFunction(player)) {
                    i++;

                    Board swapTemp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = swapTemp;
                }
            }

            Board swapTemp = arr[i + 1];
            arr[i + 1] = arr[end];
            arr[end] = swapTemp;

            return i + 1;
        }

    }

    public class UtilMove {

        private int utility;
        private Coordinate move;

        public UtilMove(int utilVal, Coordinate move) {
            this.utility = utilVal;
            this.move = move;
        }

        public int getUtility() {
            return this.utility;
        }

        public Coordinate getMove() {
            return this.move;
        }
    }
}

/*
//Our original version, but it actually kinda works, not as good as the above one though.
import java.util.ArrayList;

public class AI {

    public Coordinate nextMove(Board board, int computerTile, int maxDepth) {
        ArrayList<Coordinate> openSpots = board.getOpenSpots();
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        UtilMove bestMove = new UtilMove(Integer.MIN_VALUE, null);
        for (Coordinate point : openSpots) {
            UtilMove currentMove = this.minValue(board, openSpots, alpha, beta, Board.getNextPlayer(computerTile), maxDepth); // We want to maximize the mins for this player. 
            if (currentMove.getUtility() > bestMove.getUtility()) { // If this move is better
                bestMove = currentMove;
            }
        }
        return bestMove.getMove();
    }

    private UtilMove maxValue(Board boardState, ArrayList<Coordinate> moves, int alpha, int beta, int currentPlayer, int depth) {
        if (boardState.isGoalState() || depth <= 0) { // If this is a goal state.
            return new UtilMove(boardState.evaluationFunction(Board.getNextPlayer(currentPlayer)), null);
        }
        Coordinate action = null;
        for (Coordinate move : moves) {
            Board tempBoard = boardState.move(move, currentPlayer);
            //System.out.println("Max: Examining the move: " + move);
            int minVal = minValue(tempBoard, moves, alpha, Integer.MAX_VALUE, currentPlayer, depth - 1).getUtility();
            if (TicTacToe.ENABLE_AB_PRUNING) {
                if (minVal > beta) { // Prune
                    return new UtilMove(minVal, move);
                }
            }
            if (minVal > alpha) {
                action = move;
                alpha = minVal;
            }
        }
        return new UtilMove(alpha, action);
    }

    private UtilMove minValue(Board boardState, ArrayList<Coordinate> moves, int alpha, int beta, int currentPlayer, int depth) {
        if (boardState.isGoalState() || depth <= 0) {
            return new UtilMove(boardState.evaluationFunction(Board.getNextPlayer(currentPlayer)), null);
        }
        Coordinate action = null;
        for (Coordinate move : moves) {
            Board tempBoard = boardState.move(move, currentPlayer);
            //System.out.println("Min: Examining the move: " + move);
            int maxVal = maxValue(tempBoard, moves, Integer.MIN_VALUE, beta, currentPlayer, depth - 1).getUtility();
            if (TicTacToe.ENABLE_AB_PRUNING) {
                if (maxVal < alpha) { // Pruning
                    return new UtilMove(maxVal, move);
                }
            }
            if (maxVal < beta) {
                beta = maxVal;
                action = move;
            }
        }
        return new UtilMove(beta, action);
    }

    private ArrayList<Board> orderMoves(ArrayList<Board> boards, int player) {
        boards.sort((Board first, Board second) -> {
            if (first.evaluationFunction(player) < second.evaluationFunction(player)) {
                return 1;
            } else if (first.evaluationFunction(player) > second.evaluationFunction(player)) {
                return -1;
            } else {
                return 0; // Same score
            }
        });
        return boards;
    }

    private class UtilMove {

        private int utility;
        private Coordinate move;

        public UtilMove(int utilVal, Coordinate move) {
            this.utility = utilVal;
            this.move = move;
        }

        public int getUtility() {
            return this.utility;
        }

        public Coordinate getMove() {
            return this.move;
        }
    }
}
 */
