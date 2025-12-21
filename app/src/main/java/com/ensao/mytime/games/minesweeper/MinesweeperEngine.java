package com.ensao.mytime.games.minesweeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinesweeperEngine {
    private final int rows;
    private final int cols;
    private final int totalMines;

    private Cell[][] board;
    private boolean isGameOver;
    private boolean isGameWon;
    private boolean isFirstClick;
    private int flagsPlaced;
    private int cellsRevealed;
    private OnGameStartListener gameStartListener;

    public static class Cell {
        boolean isMine;
        boolean isRevealed;
        boolean isFlagged;
        int neighborMines;

        public Cell() {
            this.isMine = false;
            this.isRevealed = false;
            this.isFlagged = false;
            this.neighborMines = 0;
        }
    }

    public interface OnGameStartListener {
        void onGameStart();
    }

    public MinesweeperEngine(Difficulty difficulty) {
        this.rows = difficulty.getRows();
        this.cols = difficulty.getCols();
        this.totalMines = difficulty.getMines();
        reset();
    }

    public void setGameStartListener(OnGameStartListener listener) {
        this.gameStartListener = listener;
    }

    public void reset() {
        board = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c] = new Cell();
            }
        }
        isGameOver = false;
        isGameWon = false;
        isFirstClick = true;
        flagsPlaced = 0;
        cellsRevealed = 0;
    }

    private void placeMines(int excludeRow, int excludeCol) {
        Random random = new Random();
        int minesPlaced = 0;

        // Create list of all valid positions
        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Exclude first clicked cell and its neighbors for safety
                if (Math.abs(r - excludeRow) <= 1 && Math.abs(c - excludeCol) <= 1) {
                    continue;
                }
                positions.add(new int[] { r, c });
            }
        }

        // Shuffle and place mines
        while (minesPlaced < totalMines && !positions.isEmpty()) {
            int idx = random.nextInt(positions.size());
            int[] pos = positions.remove(idx);
            board[pos[0]][pos[1]].isMine = true;
            minesPlaced++;
        }

        calculateNeighborMines();
    }

    private void calculateNeighborMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!board[r][c].isMine) {
                    board[r][c].neighborMines = countMinesAround(r, c);
                }
            }
        }
    }

    private int countMinesAround(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0)
                    continue;
                int nr = r + dr;
                int nc = c + dc;
                if (isValid(nr, nc) && board[nr][nc].isMine) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public boolean reveal(int r, int c) {
        if (isGameOver || !isValid(r, c)) {
            return false;
        }

        Cell cell = board[r][c];

        // Can't reveal flagged or already revealed cells
        if (cell.isFlagged || cell.isRevealed) {
            return false;
        }

        // Handle first click
        if (isFirstClick) {
            placeMines(r, c);
            isFirstClick = false;
            if (gameStartListener != null) {
                gameStartListener.onGameStart();
            }
        }

        // Reveal the cell
        cell.isRevealed = true;
        cellsRevealed++;

        // Check if hit a mine
        if (cell.isMine) {
            isGameOver = true;
            isGameWon = false;
            revealAllMines();
            return true;
        }

        // If cell has no neighboring mines, reveal adjacent cells (flood fill)
        if (cell.neighborMines == 0) {
            revealNeighbors(r, c);
        }

        checkWinCondition();
        return true;
    }

    private void revealNeighbors(int r, int c) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0)
                    continue;
                int nr = r + dr;
                int nc = c + dc;
                if (isValid(nr, nc)) {
                    Cell neighbor = board[nr][nc];
                    if (!neighbor.isRevealed && !neighbor.isFlagged && !neighbor.isMine) {
                        reveal(nr, nc);
                    }
                }
            }
        }
    }

    public void toggleFlag(int r, int c) {
        if (isGameOver || !isValid(r, c)) {
            return;
        }

        Cell cell = board[r][c];

        // Can't flag revealed cells
        if (cell.isRevealed) {
            return;
        }

        // Toggle flag
        if (cell.isFlagged) {
            // Remove flag
            cell.isFlagged = false;
            flagsPlaced--;
        } else {
            // Add flag only if we have flags remaining
            if (flagsPlaced < totalMines) {
                cell.isFlagged = true;
                flagsPlaced++;
            }
        }
    }

    private void checkWinCondition() {
        int totalCells = rows * cols;
        if (cellsRevealed == totalCells - totalMines) {
            isGameOver = true;
            isGameWon = true;
            // Auto-flag all remaining mines
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (board[r][c].isMine && !board[r][c].isFlagged) {
                        board[r][c].isFlagged = true;
                    }
                }
            }
        }
    }

    private void revealAllMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c].isMine) {
                    board[r][c].isRevealed = true;
                }
            }
        }
    }

    public Cell getCell(int r, int c) {
        if (isValid(r, c)) {
            return board[r][c];
        }
        return null;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public boolean isGameWon() {
        return isGameWon;
    }

    public int getFlagsPlaced() {
        return flagsPlaced;
    }

    public int getTotalMines() {
        return totalMines;
    }

    public int getRemainingFlags() {
        return totalMines - flagsPlaced;
    }

    public boolean isFirstClick() {
        return isFirstClick;
    }
}
