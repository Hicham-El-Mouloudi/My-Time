package com.aurora.myapplication;

import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PuzzleGrid {
    private final String TAG = "PuzzleGrid";
    private final PuzzleNode[][] grid;
    public final int rows;
    public final int cols;

    public PuzzleGrid(int rows, int cols) {
        Log.d(TAG,"initializing grid ..");
        this.rows = rows;
        this.cols = cols;
        grid = new PuzzleNode[rows][cols];
        initializeGrid();
        Log.i(TAG,"grid initialized");
    }

    private void initializeGrid() {
        int idCounter = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                PuzzlePiece piece = new PuzzlePiece(r, c, idCounter++);
                grid[r][c] = new PuzzleNode(piece);
            }
        }
    }

    public PuzzleNode getNode(int r, int c) {
        if (isValid(r, c)) {
            return grid[r][c];
        }
        return null;
    }

    public void swap(int r1, int c1, int r2, int c2) {
        if (!isValid(r1, c1) || !isValid(r2, c2))
            return;

        PuzzlePiece temp = grid[r1][c1].getPiece();
        grid[r1][c1].setPiece(grid[r2][c2].getPiece());
        grid[r2][c2].setPiece(temp);
    }

    public void shuffle() {
        Random rand = new Random();
        for (int i = 0; i < rows * cols * 2; i++) {
            int r1 = rand.nextInt(rows);
            int c1 = rand.nextInt(cols);
            int r2 = rand.nextInt(rows);
            int c2 = rand.nextInt(cols);
            swap(r1, c1, r2, c2);
        }
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // Logic to check if neighbors are "correct" (connected in the original image)
    public boolean isCorrectNeighbor(int r, int c, Direction dir) {
        if (!isValid(r, c))
            return false;

        PuzzleNode current = grid[r][c];
        PuzzleNode neighbor;

        int targetRow = r;
        int targetCol = c;

        switch (dir) {
            case UP:
                targetRow--;
                break;
            case DOWN:
                targetRow++;
                break;
            case LEFT:
                targetCol--;
                break;
            case RIGHT:
                targetCol++;
                break;
        }

        if (targetCol>=cols || targetCol<0 || targetRow>=rows || targetRow<0)
            return false; // No neighbor in that direction (edge of board)
        neighbor = grid[targetRow][targetCol];
        // Check if the piece currently in the neighbor node is the one that SHOULD be
        // there
        // relative to the piece in the current node.

        PuzzlePiece currentPiece = current.getPiece();
        PuzzlePiece neighborPiece = neighbor.getPiece();

        if (currentPiece == null || neighborPiece == null)
            return false;

        // Expected original position of the neighbor relative to the current piece
        int expectedOriginalRow = currentPiece.originalRow;
        int expectedOriginalCol = currentPiece.originalCol;

        switch (dir) {
            case UP:
                expectedOriginalRow--;
                break;
            case DOWN:
                expectedOriginalRow++;
                break;
            case LEFT:
                expectedOriginalCol--;
                break;
            case RIGHT:
                expectedOriginalCol++;
                break;
        }

        return neighborPiece.originalRow == expectedOriginalRow &&
                neighborPiece.originalCol == expectedOriginalCol;
    }

    /**
     * Find all pieces that are correctly connected to the piece at (r, c)
     * Uses flood-fill to detect groups of correctly-placed adjacent pieces
     */
    public List<Position> findConnectedGroup(int r, int c) {
        List<Position> group = new ArrayList<>();
        Set<Position> visited = new HashSet<>();

        if (!isValid(r, c)) {
            return group;
        }

        floodFill(r, c, visited, group);

        Log.i(TAG, "Found group of size " + group.size() + " starting from (" + r + "," + c + ")");
        return group;
    }

    private void floodFill(int r, int c, Set<Position> visited, List<Position> group) {
        Position pos = new Position(r, c);

        if (visited.contains(pos) || !isValid(r, c)) {
            return;
        }

        visited.add(pos);
        group.add(pos);

        // Check all four directions
        if (isCorrectNeighbor(r, c, Direction.UP)) {
            floodFill(r - 1, c, visited, group);
        }
        if (isCorrectNeighbor(r, c, Direction.DOWN)) {
            floodFill(r + 1, c, visited, group);
        }
        if (isCorrectNeighbor(r, c, Direction.LEFT)) {
            floodFill(r, c - 1, visited, group);
        }
        if (isCorrectNeighbor(r, c, Direction.RIGHT)) {
            floodFill(r, c + 1, visited, group);
        }
    }

    public boolean isSolved() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                PuzzleNode node = grid[r][c];
                if (node == null || node.getPiece() == null)
                    return false;
                PuzzlePiece piece = node.getPiece();
                // Check if piece is in its original position
                if (piece.originalRow != r || piece.originalCol != c) {
                    return false;
                }
            }
        }
        return true;
    }

    public static class HintCandidate {
        public PuzzlePiece piece1;
        public PuzzlePiece piece2;
        public Direction direction; // Direction from piece1 to piece2 (RIGHT or DOWN)

        public HintCandidate(PuzzlePiece p1, PuzzlePiece p2, Direction dir) {
            this.piece1 = p1;
            this.piece2 = p2;
            this.direction = dir;
        }
    }

    public HintCandidate findHintPair() {
        List<HintCandidate> candidates = new ArrayList<>();

        // Map to quickly find current position of each piece
        // Key: originalRow * cols + originalCol (ID), Value: Position
        Position[] piecePositions = new Position[rows * cols];
        PuzzlePiece[] pieces = new PuzzlePiece[rows * cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                PuzzleNode node = grid[r][c];
                if (node != null && node.getPiece() != null) {
                    PuzzlePiece p = node.getPiece();
                    int id = p.originalRow * cols + p.originalCol;
                    piecePositions[id] = new Position(r, c);
                    pieces[id] = p;
                }
            }
        }

        // Check horizontal pairs
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 1; c++) {
                int id1 = r * cols + c;
                int id2 = r * cols + (c + 1);

                Position pos1 = piecePositions[id1];
                Position pos2 = piecePositions[id2];

                if (pos1 != null && pos2 != null) {
                    // Check if they are NOT correctly adjacent
                    boolean connected = (pos1.row == pos2.row && pos1.col + 1 == pos2.col);
                    if (!connected) {
                        candidates.add(new HintCandidate(pieces[id1], pieces[id2], Direction.RIGHT));
                    }
                }
            }
        }

        // Check vertical pairs
        for (int r = 0; r < rows - 1; r++) {
            for (int c = 0; c < cols; c++) {
                int id1 = r * cols + c;
                int id2 = (r + 1) * cols + c;

                Position pos1 = piecePositions[id1];
                Position pos2 = piecePositions[id2];

                if (pos1 != null && pos2 != null) {
                    // Check if they are NOT correctly adjacent
                    boolean connected = (pos1.col == pos2.col && pos1.row + 1 == pos2.row);
                    if (!connected) {
                        candidates.add(new HintCandidate(pieces[id1], pieces[id2], Direction.DOWN));
                    }
                }
            }
        }

        if (candidates.isEmpty())
            return null;
        return candidates.get(new Random().nextInt(candidates.size()));
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}
