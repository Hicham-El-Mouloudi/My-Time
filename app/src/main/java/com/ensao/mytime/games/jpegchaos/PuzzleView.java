package com.ensao.mytime.games.jpegchaos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PuzzleView extends View {

    private PuzzleGrid grid;
    private Bitmap masterBitmap;
    private Paint paint;
    private Paint borderPaint;
    private int gap = 2; // Gap between unconnected pieces
    private float cornerRadius = 8f; // Corner radius for pieces
    private float borderWidth = 2f; // Border width

    // Grid dimensions
    private int cellWidth;
    private int cellHeight;

    // Source image slice dimensions
    private int srcSliceWidth;
    private int srcSliceHeight;

    // Drag state
    private int dragRow = -1;
    private int dragCol = -1;
    private float dragX;
    private float dragY;
    private float dragStartX, dragStartY; // Initial touch position
    private boolean isDragging = false;
    private List<Position> draggedGroup = new ArrayList<>(); // Group of pieces being dragged

    // Animation state
    private boolean isAnimating = false;
    private List<AnimatingPiece> animatingPieces = new ArrayList<>(); // Multiple pieces can animate
    private float animProgress = 0f; // Shared animation progress 0-1

    // Win listener
    private OnWinListener onWinListener;

    public interface OnWinListener {
        void onWin();
    }

    public void setOnWinListener(OnWinListener listener) {
        this.onWinListener = listener;
    }

    // Sound listener
    private OnSoundListener onSoundListener;

    public interface OnSoundListener {
        void onDrag();

        void onDrop();

        void onUnite();

        void onHint();
    }

    public void setOnSoundListener(OnSoundListener listener) {
        this.onSoundListener = listener;
    }

    public PuzzleView(Context context) {
        super(context);
        init();
    }

    public PuzzleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Helper class for tracking animating pieces
    private static class AnimatingPiece {
        PuzzlePiece piece;
        int srcRow, srcCol;
        int dstRow, dstCol;
        float startOffsetX = 0f; // Offset for dragged pieces
        float startOffsetY = 0f;

        AnimatingPiece(PuzzlePiece piece, int srcRow, int srcCol, int dstRow, int dstCol) {
            this.piece = piece;
            this.srcRow = srcRow;
            this.srcCol = srcCol;
            this.dstRow = dstRow;
            this.dstCol = dstCol;
        }
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(0x88000000); // Semi-transparent black

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f); // Reduced size
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(10f, 0, 0, Color.BLACK);

        shadowPaint = new Paint();
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(PuzzleGrid grid, Bitmap masterBitmap) {
        this.grid = grid;
        this.masterBitmap = masterBitmap;
        if (masterBitmap != null && grid != null) {
            this.srcSliceWidth = masterBitmap.getWidth() / grid.cols;
            this.srcSliceHeight = masterBitmap.getHeight() / grid.rows;
        }
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (masterBitmap == null || grid == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        float aspectRatio = (float) masterBitmap.getWidth() / masterBitmap.getHeight();

        // Width is constrained, calculate height
        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            width = widthSize;
            height = (int) (width / aspectRatio);

            // If height constraint exists and we exceed it, scale down
            if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
                height = heightSize;
                width = (int) (height * aspectRatio);
            } else if (heightMode == MeasureSpec.EXACTLY) {
                // Both dimensions are exact, fit inside while preserving aspect ratio
                if (widthSize / (float) heightSize > aspectRatio) {
                    height = heightSize;
                    width = (int) (height * aspectRatio);
                } else {
                    width = widthSize;
                    height = (int) (width / aspectRatio);
                }
            }
        } else {
            // No width constraint, use bitmap size
            width = masterBitmap.getWidth();
            height = masterBitmap.getHeight();
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (grid == null || masterBitmap == null)
            return;

        int width = getWidth();
        int height = getHeight();
        int rows = grid.rows;
        int cols = grid.cols;

        // Calculate cell size based on view size
        cellWidth = width / cols;
        cellHeight = height / rows;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Skip drawing pieces in the dragged group
                if (isDragging && isInGroup(r, c, draggedGroup))
                    continue;

                // Skip drawing destination positions of animating pieces
                if (isAnimating && isAnimatingDestination(r, c))
                    continue;

                PuzzleNode node = grid.getNode(r, c);
                if (node == null)
                    continue;

                PuzzlePiece piece = node.getPiece();
                if (piece == null)
                    continue;

                drawPiece(canvas, r, c, piece, r * cellHeight, c * cellWidth);
            }
        }

        // Draw all animating pieces
        if (isAnimating && !animatingPieces.isEmpty()) {
            for (AnimatingPiece anim : animatingPieces) {
                // Starting position includes drag offset for dragged pieces
                float startX = anim.srcCol * cellWidth + anim.startOffsetX;
                float startY = anim.srcRow * cellHeight + anim.startOffsetY;
                float endX = anim.dstCol * cellWidth;
                float endY = anim.dstRow * cellHeight;

                float currentX = startX + (endX - startX) * animProgress;
                float currentY = startY + (endY - startY) * animProgress;

                drawPieceAt(canvas, anim.piece, currentX, currentY);
            }
        }

        // Draw dragged group on top
        if (isDragging && !draggedGroup.isEmpty()) {

            // Calculate offset from drag start
            float offsetX = dragX - dragStartX;
            float offsetY = dragY - dragStartY;

            for (Position pos : draggedGroup) {
                PuzzleNode node = grid.getNode(pos.row, pos.col);
                if (node != null) {
                    PuzzlePiece piece = node.getPiece();
                    if (piece != null) {
                        float drawX = pos.col * cellWidth + offsetX;
                        float drawY = pos.row * cellHeight + offsetY;
                        drawPieceAt(canvas, piece, drawX, drawY);
                    }
                }
            }
        }

        // Draw hint overlay
        if (currentHint != null) {
            drawHintPiece(canvas, currentHint.piece1, currentHint.text1, currentHint.alpha);
            drawHintPiece(canvas, currentHint.piece2, currentHint.text2, currentHint.alpha);
        }
    }

    private boolean isInGroup(int r, int c, List<Position> group) {
        for (Position pos : group) {
            if (pos.row == r && pos.col == c) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnimatingDestination(int r, int c) {
        for (AnimatingPiece anim : animatingPieces) {
            if (anim.dstRow == r && anim.dstCol == c) {
                return true;
            }
        }
        return false;
    }

    private void drawPieceAt(Canvas canvas, PuzzlePiece piece, float x, float y) {
        // For dragged/animating pieces, we don't apply the "gap" logic because they are
        // floating.
        // Or maybe we should? Let's keep it simple and just draw the full tile.

        Rect src = new Rect(
                piece.originalCol * srcSliceWidth,
                piece.originalRow * srcSliceHeight,
                (piece.originalCol + 1) * srcSliceWidth,
                (piece.originalRow + 1) * srcSliceHeight);

        RectF dst = new RectF(x, y, x + cellWidth, y + cellHeight);
        canvas.drawBitmap(masterBitmap, src, dst, paint);
    }

    private void drawPiece(Canvas canvas, int r, int c, PuzzlePiece piece, int top, int left) {
        int paddingLeft = 0;
        int paddingTop = 0;
        int paddingRight = 0;
        int paddingBottom = 0;

        // Track which edges need borders (disconnected edges)
        boolean borderLeft = false;
        boolean borderRight = false;
        boolean borderTop = false;
        boolean borderBottom = false;

        // Check neighbors to determine padding/gaps and borders
        if (!grid.isCorrectNeighbor(r, c, PuzzleGrid.Direction.LEFT)) {
            paddingLeft = gap;
            borderLeft = true;
        }
        if (!grid.isCorrectNeighbor(r, c, PuzzleGrid.Direction.RIGHT)) {
            paddingRight = gap;
            borderRight = true;
        }
        if (!grid.isCorrectNeighbor(r, c, PuzzleGrid.Direction.UP)) {
            paddingTop = gap;
            borderTop = true;
        }
        if (!grid.isCorrectNeighbor(r, c, PuzzleGrid.Direction.DOWN)) {
            paddingBottom = gap;
            borderBottom = true;
        }

        // Edge cases: board edges always have gaps and borders
        if (c == 0) {
            paddingLeft = gap;
            borderLeft = true;
        }
        if (c == grid.cols - 1) {
            paddingRight = gap;
            borderRight = true;
        }
        if (r == 0) {
            paddingTop = gap;
            borderTop = true;
        }
        if (r == grid.rows - 1) {
            paddingBottom = gap;
            borderBottom = true;
        }

        Rect src = new Rect(
                piece.originalCol * srcSliceWidth,
                piece.originalRow * srcSliceHeight,
                (piece.originalCol + 1) * srcSliceWidth,
                (piece.originalRow + 1) * srcSliceHeight);

        RectF dst = new RectF(
                left + paddingLeft,
                top + paddingTop,
                left + cellWidth - paddingRight,
                top + cellHeight - paddingBottom);

        // Save canvas state
        canvas.save();

        // Calculate dynamic corner radii
        // A corner is sharp (0 radius) if EITHER adjacent side has a neighbor (no
        // border)
        float tl = (!borderLeft || !borderTop) ? 0 : cornerRadius;
        float tr = (!borderTop || !borderRight) ? 0 : cornerRadius;
        float br = (!borderRight || !borderBottom) ? 0 : cornerRadius;
        float bl = (!borderBottom || !borderLeft) ? 0 : cornerRadius;

        float[] radii = new float[] { tl, tl, tr, tr, br, br, bl, bl };

        // Clip to rounded rectangle with dynamic radii
        android.graphics.Path clipPath = new android.graphics.Path();
        clipPath.addRoundRect(dst, radii, android.graphics.Path.Direction.CW);
        canvas.clipPath(clipPath);

        // Draw the bitmap
        canvas.drawBitmap(masterBitmap, src, dst, paint);

        // Restore canvas
        canvas.restore();

        // Draw borders only on disconnected edges
        if (borderLeft || borderRight || borderTop || borderBottom) {
            android.graphics.Path borderPath = new android.graphics.Path();

            float halfBorder = borderWidth / 2f;
            RectF borderRect = new RectF(
                    dst.left + halfBorder,
                    dst.top + halfBorder,
                    dst.right - halfBorder,
                    dst.bottom - halfBorder);

            // Left Border
            if (borderLeft) {
                float startY = borderRect.bottom - (borderBottom ? cornerRadius : 0);
                borderPath.moveTo(borderRect.left, startY);

                if (borderTop) {
                    borderPath.lineTo(borderRect.left, borderRect.top + cornerRadius);
                    borderPath.quadTo(borderRect.left, borderRect.top, borderRect.left + cornerRadius, borderRect.top);
                } else {
                    borderPath.lineTo(borderRect.left, borderRect.top);
                }
            }

            // Top Border
            if (borderTop) {
                if (!borderLeft) {
                    borderPath.moveTo(borderRect.left, borderRect.top);
                }

                if (borderRight) {
                    borderPath.lineTo(borderRect.right - cornerRadius, borderRect.top);
                    borderPath.quadTo(borderRect.right, borderRect.top, borderRect.right,
                            borderRect.top + cornerRadius);
                } else {
                    borderPath.lineTo(borderRect.right, borderRect.top);
                }
            }

            // Right Border
            if (borderRight) {
                if (!borderTop) {
                    borderPath.moveTo(borderRect.right, borderRect.top);
                }

                if (borderBottom) {
                    borderPath.lineTo(borderRect.right, borderRect.bottom - cornerRadius);
                    borderPath.quadTo(borderRect.right, borderRect.bottom, borderRect.right - cornerRadius,
                            borderRect.bottom);
                } else {
                    borderPath.lineTo(borderRect.right, borderRect.bottom);
                }
            }

            // Bottom Border
            if (borderBottom) {
                if (!borderRight) {
                    borderPath.moveTo(borderRect.right, borderRect.bottom);
                }

                if (borderLeft) {
                    borderPath.lineTo(borderRect.left + cornerRadius, borderRect.bottom);
                    borderPath.quadTo(borderRect.left, borderRect.bottom, borderRect.left,
                            borderRect.bottom - cornerRadius);
                } else {
                    borderPath.lineTo(borderRect.left, borderRect.bottom);
                }
            }

            canvas.drawPath(borderPath, borderPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (grid == null || isAnimating)
            return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int c = (int) (x / cellWidth);
                int r = (int) (y / cellHeight);

                if (r >= 0 && r < grid.rows && c >= 0 && c < grid.cols) {
                    dragRow = r;
                    dragCol = c;
                    dragX = x;
                    dragY = y;
                    dragStartX = x;
                    dragStartY = y;
                    isDragging = true;

                    // Find all connected pieces in the group
                    draggedGroup = grid.findConnectedGroup(r, c);

                    if (onSoundListener != null) {
                        onSoundListener.onDrag();
                    }

                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    dragX = x;
                    dragY = y;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    int targetC = (int) (x / cellWidth);
                    int targetR = (int) (y / cellHeight);

                    if (targetR >= 0 && targetR < grid.rows && targetC >= 0 && targetC < grid.cols) {
                        if (targetR != dragRow || targetC != dragCol) {
                            // Calculate offset for group movement
                            int offsetRow = targetR - dragRow;
                            int offsetCol = targetC - dragCol;

                            // Calculate current visual drag offset in pixels
                            float dragOffsetX = dragX - dragStartX;
                            float dragOffsetY = dragY - dragStartY;

                            performGroupSwap(draggedGroup, offsetRow, offsetCol, dragOffsetX, dragOffsetY);
                        }
                    }

                    if (onSoundListener != null) {
                        onSoundListener.onDrop();
                    }

                    isDragging = false;
                    dragRow = -1;
                    dragCol = -1;
                    draggedGroup.clear(); // Clear the dragged group
                    invalidate();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);

    }

    private void performGroupSwap(List<Position> group, int offsetRow, int offsetCol, float dragOffsetX,
            float dragOffsetY) {
        if (group.isEmpty())
            return;

        // Step 1: Calculate all target positions and validate
        List<Position> targetPositions = new ArrayList<>();
        for (Position pos : group) {
            int targetRow = pos.row + offsetRow;
            int targetCol = pos.col + offsetCol;

            if (targetRow < 0 || targetRow >= grid.rows || targetCol < 0 || targetCol >= grid.cols) {
                return;
            }
            targetPositions.add(new Position(targetRow, targetCol));
        }

        // Step 2: Find displaced pieces
        List<Position> displacedPositions = new ArrayList<>();
        for (Position targetPos : targetPositions) {
            boolean isInGroup = false;
            for (Position groupPos : group) {
                if (groupPos.row == targetPos.row && groupPos.col == targetPos.col) {
                    isInGroup = true;
                    break;
                }
            }
            if (!isInGroup) {
                displacedPositions.add(targetPos);
            }
        }

        // Step 3: Save pieces
        List<PuzzlePiece> savedGroupPieces = new ArrayList<>();
        for (Position pos : group) {
            PuzzleNode node = grid.getNode(pos.row, pos.col);
            if (node != null && node.getPiece() != null) {
                savedGroupPieces.add(node.getPiece());
            }
        }

        List<PuzzlePiece> savedDisplacedPieces = new ArrayList<>();
        for (Position pos : displacedPositions) {
            PuzzleNode node = grid.getNode(pos.row, pos.col);
            if (node != null && node.getPiece() != null) {
                savedDisplacedPieces.add(node.getPiece());
            }
        }

        // Step 4: Clear positions
        for (Position pos : group) {
            PuzzleNode node = grid.getNode(pos.row, pos.col);
            if (node != null)
                node.setPiece(null);
        }
        for (Position pos : displacedPositions) {
            PuzzleNode node = grid.getNode(pos.row, pos.col);
            if (node != null)
                node.setPiece(null);
        }

        // Step 5: Place group pieces at new positions
        for (int i = 0; i < savedGroupPieces.size(); i++) {
            Position targetPos = targetPositions.get(i);
            PuzzleNode node = grid.getNode(targetPos.row, targetPos.col);
            if (node != null) {
                node.setPiece(savedGroupPieces.get(i));
            }
        }

        // Step 6: Place displaced pieces
        int displacedIndex = 0;
        for (Position groupPos : group) {
            PuzzleNode node = grid.getNode(groupPos.row, groupPos.col);
            if (node != null && node.getPiece() == null && displacedIndex < savedDisplacedPieces.size()) {
                node.setPiece(savedDisplacedPieces.get(displacedIndex));
                displacedIndex++;
            }
        }

        // Step 7: Animate ALL pieces
        animatingPieces.clear();

        // Add dragged pieces with drag offset
        for (int i = 0; i < group.size() && i < targetPositions.size(); i++) {
            Position srcPos = group.get(i);
            Position dstPos = targetPositions.get(i);
            PuzzlePiece piece = savedGroupPieces.get(i);

            AnimatingPiece anim = new AnimatingPiece(piece, srcPos.row, srcPos.col, dstPos.row, dstPos.col);
            anim.startOffsetX = dragOffsetX;
            anim.startOffsetY = dragOffsetY;
            animatingPieces.add(anim);
        }

        // Add displaced pieces
        displacedIndex = 0;
        for (Position groupPos : group) {
            PuzzleNode node = grid.getNode(groupPos.row, groupPos.col);
            if (node != null && displacedIndex < savedDisplacedPieces.size()) {
                PuzzlePiece piece = node.getPiece();
                for (int i = 0; i < savedDisplacedPieces.size(); i++) {
                    if (piece == savedDisplacedPieces.get(i)) {
                        Position srcPos = displacedPositions.get(i);
                        animatingPieces
                                .add(new AnimatingPiece(piece, srcPos.row, srcPos.col, groupPos.row, groupPos.col));
                        displacedIndex++;
                        break;
                    }
                }
            }
        }

        // Start animation
        if (!animatingPieces.isEmpty()) {
            isAnimating = true;
            animProgress = 0f;

            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(400);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                animProgress = (float) animation.getAnimatedValue();
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isAnimating = false;
                    animatingPieces.clear();
                    animProgress = 0f;
                    invalidate();

                    // Check for win condition
                    if (grid != null && grid.isSolved() && onWinListener != null) {
                        onWinListener.onWin();
                    }
                }
            });
            animator.start();
        } else {
            invalidate();
        }

        // Check for unite sound
        // We can check if the group size increased
        if (onSoundListener != null && !targetPositions.isEmpty()) {
            Position firstTarget = targetPositions.get(0);
            List<Position> newGroup = grid.findConnectedGroup(firstTarget.row, firstTarget.col);
            if (newGroup.size() > group.size()) {
                onSoundListener.onUnite();
            }
        }
    }

    private void performSwap(int r1, int c1, int r2, int c2) {
        final PuzzlePiece swapTargetPiece = grid.getNode(r2, c2).getPiece();

        grid.swap(r1, c1, r2, c2);

        // Create single-piece animation using the new system
        animatingPieces.clear();
        animatingPieces.add(new AnimatingPiece(swapTargetPiece, r2, c2, r1, c1));

        isAnimating = true;
        animProgress = 0f;

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                animatingPieces.clear();
                animProgress = 0f;
                invalidate();

                // Check for win condition
                if (grid != null && grid.isSolved() && onWinListener != null) {
                    onWinListener.onWin();
                }
            }
        });
        animator.start();
    }

    // Hint state
    private HintState currentHint;
    private ValueAnimator hintAnimator;
    private Paint textPaint;
    private Paint shadowPaint;

    private class HintState {
        PuzzlePiece piece1;
        PuzzlePiece piece2;
        String text1;
        String text2;
        float alpha;
    }

    public boolean showHint() {
        if (grid == null)
            return false;
        PuzzleGrid.HintCandidate candidate = grid.findHintPair();
        if (candidate == null)
            return false;

        if (onSoundListener != null) {
            onSoundListener.onHint();
        }

        currentHint = new HintState();
        currentHint.piece1 = candidate.piece1;
        currentHint.piece2 = candidate.piece2;
        currentHint.alpha = 1f;

        if (candidate.direction == PuzzleGrid.Direction.RIGHT) {
            currentHint.text1 = "LEFT";
            currentHint.text2 = "RIGHT";
        } else { // DOWN
            currentHint.text1 = "TOP";
            currentHint.text2 = "BOTTOM";
        }

        if (hintAnimator != null) {
            hintAnimator.cancel();
        }

        // Animate fade out
        hintAnimator = ValueAnimator.ofFloat(1f, 0f);
        hintAnimator.setDuration(2000); // 2 seconds fade
        hintAnimator.setStartDelay(1000); // Keep visible for 1s before fading
        hintAnimator.addUpdateListener(animation -> {
            if (currentHint != null) {
                currentHint.alpha = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        hintAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentHint = null;
                invalidate();
            }
        });
        hintAnimator.start();

        invalidate();
        return true;
    }

    private void drawHintPiece(Canvas canvas, PuzzlePiece piece, String text, float alpha) {
        if (piece == null)
            return;

        RectF rect = getPieceVisualRect(piece);
        if (rect == null)
            return;

        // Draw shadow overlay
        shadowPaint.setAlpha((int) (150 * alpha)); // Max alpha 150
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, shadowPaint);

        // Draw text
        textPaint.setAlpha((int) (255 * alpha));

        // Center text
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float x = rect.centerX();
        float y = rect.centerY() - (metrics.descent + metrics.ascent) / 2;

        canvas.drawText(text, x, y, textPaint);
    }

    private RectF getPieceVisualRect(PuzzlePiece piece) {
        // Check if piece is being dragged
        if (isDragging) {
            for (Position pos : draggedGroup) {
                PuzzleNode node = grid.getNode(pos.row, pos.col);
                if (node != null && node.getPiece() == piece) {
                    float offsetX = dragX - dragStartX;
                    float offsetY = dragY - dragStartY;
                    float left = pos.col * cellWidth + offsetX;
                    float top = pos.row * cellHeight + offsetY;
                    return new RectF(left, top, left + cellWidth, top + cellHeight);
                }
            }
        }

        // Check if piece is animating
        if (isAnimating) {
            for (AnimatingPiece anim : animatingPieces) {
                if (anim.piece == piece) {
                    float startX = anim.srcCol * cellWidth + anim.startOffsetX;
                    float startY = anim.srcRow * cellHeight + anim.startOffsetY;
                    float endX = anim.dstCol * cellWidth;
                    float endY = anim.dstRow * cellHeight;
                    float currentX = startX + (endX - startX) * animProgress;
                    float currentY = startY + (endY - startY) * animProgress;
                    return new RectF(currentX, currentY, currentX + cellWidth, currentY + cellHeight);
                }
            }
        }

        // Otherwise, find it in the grid
        for (int r = 0; r < grid.rows; r++) {
            for (int c = 0; c < grid.cols; c++) {
                PuzzleNode node = grid.getNode(r, c);
                if (node != null && node.getPiece() == piece) {
                    float left = c * cellWidth;
                    float top = r * cellHeight;
                    return new RectF(left, top, left + cellWidth, top + cellHeight);
                }
            }
        }
        return null;
    }
}
