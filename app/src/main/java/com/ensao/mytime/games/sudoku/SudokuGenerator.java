package com.ensao.mytime.games.sudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SudokuGenerator {

    public enum Difficulty {
        EASY(45), // 45 clues
        MEDIUM(35), // 35 clues
        HARD(28); // 28 clues

        private final int clues;

        Difficulty(int clues) {
            this.clues = clues;
        }

        public int getClues() {
            return clues;
        }
    }

    private Random random;

    public SudokuGenerator() {
        this.random = new Random();
    }

    public int[][] generateSolution() {
        int[][] board = new int[9][9];
        fillBoard(board);
        return board;
    }

    private boolean fillBoard(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    List<Integer> numbers = new ArrayList<>();
                    for (int i = 1; i <= 9; i++) {
                        numbers.add(i);
                    }
                    Collections.shuffle(numbers, random);

                    for (int num : numbers) {
                        if (SudokuSolver.isValid(board, row, col, num)) {
                            board[row][col] = num;

                            if (fillBoard(board)) {
                                return true;
                            }

                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public int[][] generatePuzzle(Difficulty difficulty) {
        int[][] solution = generateSolution();
        int[][] puzzle = new int[9][9];

        // Copy solution to puzzle
        for (int i = 0; i < 9; i++) {
            System.arraycopy(solution[i], 0, puzzle[i], 0, 9);
        }

        // Create list of all cell positions
        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells.add(new int[] { i, j });
            }
        }
        Collections.shuffle(cells, random);

        // Remove numbers to create puzzle
        int targetClues = difficulty.getClues();
        int currentClues = 81;

        for (int[] cell : cells) {
            if (currentClues <= targetClues)
                break;

            int row = cell[0];
            int col = cell[1];
            int backup = puzzle[row][col];
            puzzle[row][col] = 0;

            // Check if puzzle still has unique solution
            int[][] testBoard = new int[9][9];
            for (int i = 0; i < 9; i++) {
                System.arraycopy(puzzle[i], 0, testBoard[i], 0, 9);
            }

            if (SudokuSolver.countSolutions(testBoard) == 1) {
                currentClues--;
            } else {
                puzzle[row][col] = backup; // Restore if not unique
            }
        }

        return puzzle;
    }

    public static class PuzzleData {
        public int[][] puzzle;
        public int[][] solution;

        public PuzzleData(int[][] puzzle, int[][] solution) {
            this.puzzle = puzzle;
            this.solution = solution;
        }
    }

    public PuzzleData generate(Difficulty difficulty) {
        int[][] solution = generateSolution();
        int[][] puzzle = generatePuzzle(difficulty, solution);
        return new PuzzleData(puzzle, solution);
    }

    private int[][] generatePuzzle(Difficulty difficulty, int[][] solution) {
        int[][] puzzle = new int[9][9];

        // Copy solution to puzzle
        for (int i = 0; i < 9; i++) {
            System.arraycopy(solution[i], 0, puzzle[i], 0, 9);
        }

        // Create list of all cell positions
        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells.add(new int[] { i, j });
            }
        }
        Collections.shuffle(cells, random);

        // Remove numbers to create puzzle
        int targetClues = difficulty.getClues();
        int currentClues = 81;

        for (int[] cell : cells) {
            if (currentClues <= targetClues)
                break;

            int row = cell[0];
            int col = cell[1];
            int backup = puzzle[row][col];
            puzzle[row][col] = 0;

            // Check if puzzle still has unique solution
            int[][] testBoard = new int[9][9];
            for (int i = 0; i < 9; i++) {
                System.arraycopy(puzzle[i], 0, testBoard[i], 0, 9);
            }

            if (SudokuSolver.countSolutions(testBoard) == 1) {
                currentClues--;
            } else {
                puzzle[row][col] = backup; // Restore if not unique
            }
        }

        return puzzle;
    }
}
