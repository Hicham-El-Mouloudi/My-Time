package com.ensao.mytime.games.jpegchaos;

/**
 * encapsulate the puzzle piece for future changes
 * and to make the ui logic separated from the puzzle logic
 */
public class PuzzleNode {
    private PuzzlePiece piece;

    public PuzzleNode(PuzzlePiece piece) {
        this.piece = piece;
    }

    public PuzzlePiece getPiece() {
        return piece;
    }

    public void setPiece(PuzzlePiece piece) {
        this.piece = piece;
    }


}
