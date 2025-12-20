package com.ensao.mytime.games.sudoku;

public class SudokuSolver {

    public static boolean solve(int[][] board) {
        return solveHelper(board);
    }

    private static boolean solveHelper(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isValid(board, row, col, num)) {
                            board[row][col] = num;

                            if (solveHelper(board)) {
                                return true;
                            }

                            board[row][col] = 0; // Backtrack
                        }
                    }
                    return false; // No valid number found
                }
            }
        }
        return true; // All cells filled
    }

    public static boolean isValid(int[][] board, int row, int col, int num) {
        // Check row
        for (int j = 0; j < 9; j++) {
            if (board[row][j] == num) {
                return false;
            }
        }

        // Check column
        for (int i = 0; i < 9; i++) {
            if (board[i][col] == num) {
                return false;
            }
        }

        // Check 3x3 box
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int i = boxRow; i < boxRow + 3; i++) {
            for (int j = boxCol; j < boxCol + 3; j++) {
                if (board[i][j] == num) {
                    return false;
                }
            }
        }

        return true;
    }

    // Count number of solutions (used to verify uniqueness)
    public static int countSolutions(int[][] board) {
        return countSolutionsHelper(board, 0);
    }

    private static int countSolutionsHelper(int[][] board, int count) {
        if (count > 1)
            return count; // Early termination if more than one solution

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isValid(board, row, col, num)) {
                            board[row][col] = num;
                            count = countSolutionsHelper(board, count);
                            board[row][col] = 0;

                            if (count > 1)
                                return count;
                        }
                    }
                    return count;
                }
            }
        }
        return count + 1; // Found a solution
    }
}
