package com.ensao.mytime.games.sudoku;

import java.util.HashSet;
import java.util.Set;

public class SudokuBoard {
    private int[][] solution; // The complete solution
    private int[][] puzzle; // The puzzle with some cells empty (0)
    private int[][] userInput; // User's current input
    private boolean[][] isClue; // Track which cells are initial clues
    private Set<String>[][] notes; // Notes/candidates for each cell
    private Set<String> errorCells; // Cells with errors

    public SudokuBoard() {
        solution = new int[9][9];
        puzzle = new int[9][9];
        userInput = new int[9][9];
        isClue = new boolean[9][9];
        notes = new HashSet[9][9];
        errorCells = new HashSet<>();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                notes[i][j] = new HashSet<>();
            }
        }
    }

    public void setPuzzle(int[][] puzzle, int[][] solution) {
        this.solution = deepCopy(solution);
        this.puzzle = deepCopy(puzzle);
        this.userInput = deepCopy(puzzle);

        // Mark clues
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                isClue[i][j] = puzzle[i][j] != 0;
                notes[i][j].clear();
            }
        }
        errorCells.clear();
    }

    public int getValue(int row, int col) {
        return userInput[row][col];
    }

    public void setValue(int row, int col, int value) {
        if (!isClue[row][col]) {
            userInput[row][col] = value;
            if (value != 0) {
                notes[row][col].clear(); // Clear notes when number is placed
            }
            updateErrors();
        }
    }

    public boolean isClue(int row, int col) {
        return isClue[row][col];
    }

    public Set<String> getNotes(int row, int col) {
        return notes[row][col];
    }

    public void toggleNote(int row, int col, int value) {
        if (!isClue[row][col] && userInput[row][col] == 0) {
            String note = String.valueOf(value);
            if (notes[row][col].contains(note)) {
                notes[row][col].remove(note);
            } else {
                notes[row][col].add(note);
            }
        }
    }

    public void clearCell(int row, int col) {
        if (!isClue[row][col]) {
            userInput[row][col] = 0;
            notes[row][col].clear();
            updateErrors();
        }
    }

    public boolean hasError(int row, int col) {
        return errorCells.contains(row + "," + col);
    }

    private void updateErrors() {
        errorCells.clear();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (userInput[i][j] != 0 && !isValidPlacement(i, j, userInput[i][j])) {
                    errorCells.add(i + "," + j);
                }
            }
        }
    }

    private boolean isValidPlacement(int row, int col, int value) {
        // First check basic Sudoku rules to catch obvious conflicts
        // Check row
        for (int j = 0; j < 9; j++) {
            if (j != col && userInput[row][j] == value) {
                return false;
            }
        }

        // Check column
        for (int i = 0; i < 9; i++) {
            if (i != row && userInput[i][col] == value) {
                return false;
            }
        }

        // Check 3x3 box
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int i = boxRow; i < boxRow + 3; i++) {
            for (int j = boxCol; j < boxCol + 3; j++) {
                if ((i != row || j != col) && userInput[i][j] == value) {
                    return false;
                }
            }
        }

        // Most importantly: check if it matches the solution
        // This ensures the user is on the path to the unique solution
        return value == solution[row][col];
    }

    public boolean isSolved() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (userInput[i][j] == 0 || userInput[i][j] != solution[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[][] getSolution() {
        return solution;
    }

    public int[][] getUserInput() {
        return userInput;
    }

    public int getEmptyCellCount() {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (userInput[i][j] == 0)
                    count++;
            }
        }
        return count;
    }

    private int[][] deepCopy(int[][] array) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(array[i], 0, copy[i], 0, 9);
        }
        return copy;
    }
}
