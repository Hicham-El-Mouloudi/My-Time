package com.ensao.mytime.games.minesweeper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class MinesweeperView extends View {

    private MinesweeperEngine engine;
    private Paint cellPaint;
    private Paint textPaint;
    private Paint borderPaint;
    private float cellSize;
    private float offsetX, offsetY;
    private OnCellClickListener listener;
    private boolean isFlagMode = false;
    private boolean isDarkMode = true;

    // Dark mode color palette
    private static final int DARK_BG = 0xFF1A1A2E;
    private static final int DARK_UNREVEALED_START = 0xFF6C63FF;
    private static final int DARK_UNREVEALED_END = 0xFF8B80FF;
    private static final int DARK_REVEALED = 0xFF252A3E;
    private static final int DARK_MINE = 0xFFFF4757;
    private static final int DARK_FLAG = 0xFFFFA502;
    private static final int DARK_BORDER = 0xFF0F0F1E;

    // Light mode color palette
    private static final int LIGHT_BG = 0xFFF5F5F5;
    private static final int LIGHT_UNREVEALED_START = 0xFF5B8DEE;
    private static final int LIGHT_UNREVEALED_END = 0xFF7AA5FF;
    private static final int LIGHT_REVEALED = 0xFFE8E8E8;
    private static final int LIGHT_MINE = 0xFFE63946;
    private static final int LIGHT_FLAG = 0xFFFF8800;
    private static final int LIGHT_BORDER = 0xFFCCCCCC;

    // Number colors - vibrant and distinct
    private static final int[] NUMBER_COLORS = {
            0x00000000, // 0 (transparent)
            0xFF00D2FF, // 1 Cyan
            0xFF00FF88, // 2 Green
            0xFFFFD93D, // 3 Yellow
            0xFFFF6B9D, // 4 Pink
            0xFFFF6348, // 5 Red
            0xFFC44569, // 6 Purple
            0xFF4834DF, // 7 Blue
            0xFF2C3A47 // 8 Dark
    };

    public interface OnCellClickListener {
        void onCellClick(int row, int col);
    }

    public MinesweeperView(Context context) {
        super(context);
        init();
    }

    public MinesweeperView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStrokeWidth(2);
        borderPaint.setStyle(Paint.Style.STROKE);
    }

    public void setEngine(MinesweeperEngine engine) {
        this.engine = engine;
        requestLayout();
        invalidate();
    }

    public void setOnCellClickListener(OnCellClickListener listener) {
        this.listener = listener;
    }

    public void setFlagMode(boolean flagMode) {
        this.isFlagMode = flagMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
        setBackgroundColor(isDarkMode ? DARK_BG : LIGHT_BG);
    }

    private int getColorUnrevealedStart() {
        return isDarkMode ? DARK_UNREVEALED_START : LIGHT_UNREVEALED_START;
    }

    private int getColorUnrevealedEnd() {
        return isDarkMode ? DARK_UNREVEALED_END : LIGHT_UNREVEALED_END;
    }

    private int getColorRevealed() {
        return isDarkMode ? DARK_REVEALED : LIGHT_REVEALED;
    }

    private int getColorMine() {
        return isDarkMode ? DARK_MINE : LIGHT_MINE;
    }

    private int getColorFlag() {
        return isDarkMode ? DARK_FLAG : LIGHT_FLAG;
    }

    private int getColorBorder() {
        return isDarkMode ? DARK_BORDER : LIGHT_BORDER;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (engine == null)
            return;

        int rows = engine.getRows();
        int cols = engine.getCols();
        int width = getWidth();
        int height = getHeight();

        // Calculate cell size with padding
        float padding = 20f;
        float availableWidth = width - padding * 2;
        float availableHeight = height - padding * 2;

        float cellWidth = availableWidth / cols;
        float cellHeight = availableHeight / rows;
        cellSize = Math.min(cellWidth, cellHeight);

        // Center the board
        float boardWidth = cellSize * cols;
        float boardHeight = cellSize * rows;
        offsetX = (width - boardWidth) / 2;
        offsetY = (height - boardHeight) / 2;

        textPaint.setTextSize(cellSize * 0.5f);

        // Draw all cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                drawCell(canvas, r, c);
            }
        }
    }

    private void drawCell(Canvas canvas, int r, int c) {
        float x = offsetX + c * cellSize;
        float y = offsetY + r * cellSize;
        float cellPadding = 4f;
        RectF rect = new RectF(
                x + cellPadding,
                y + cellPadding,
                x + cellSize - cellPadding,
                y + cellSize - cellPadding);

        MinesweeperEngine.Cell cell = engine.getCell(r, c);
        if (cell == null)
            return;

        if (cell.isRevealed) {
            drawRevealedCell(canvas, rect, cell);
        } else {
            drawUnrevealedCell(canvas, rect, cell);
        }
    }

    private void drawUnrevealedCell(Canvas canvas, RectF rect, MinesweeperEngine.Cell cell) {
        // Draw gradient background
        LinearGradient gradient = new LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                getColorUnrevealedStart(), getColorUnrevealedEnd(),
                Shader.TileMode.CLAMP);
        cellPaint.setShader(gradient);
        canvas.drawRoundRect(rect, 12f, 12f, cellPaint);
        cellPaint.setShader(null);

        // Draw border for depth effect
        borderPaint.setColor(0x40FFFFFF);
        borderPaint.setStrokeWidth(3);
        canvas.drawRoundRect(rect, 12f, 12f, borderPaint);

        // Draw flag if flagged
        if (cell.isFlagged) {
            drawFlag(canvas, rect);
        }
    }

    private void drawRevealedCell(Canvas canvas, RectF rect, MinesweeperEngine.Cell cell) {
        // Draw flat background
        cellPaint.setColor(getColorRevealed());
        canvas.drawRoundRect(rect, 8f, 8f, cellPaint);

        // Draw subtle border
        borderPaint.setColor(getColorBorder());
        borderPaint.setStrokeWidth(2);
        canvas.drawRoundRect(rect, 8f, 8f, borderPaint);

        if (cell.isMine) {
            drawMine(canvas, rect);
        } else if (cell.neighborMines > 0) {
            drawNumber(canvas, rect, cell.neighborMines);
        }
    }

    private void drawFlag(Canvas canvas, RectF rect) {
        float centerX = rect.centerX();
        float centerY = rect.centerY();
        float size = Math.min(rect.width(), rect.height()) * 0.5f;

        // Draw flag pole
        cellPaint.setColor(0xFF2C3A47);
        cellPaint.setStrokeWidth(size * 0.1f);
        cellPaint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(
                centerX - size * 0.2f, centerY - size * 0.3f,
                centerX - size * 0.2f, centerY + size * 0.4f,
                cellPaint);

        // Draw flag
        cellPaint.setStyle(Paint.Style.FILL);
        cellPaint.setColor(getColorFlag());
        Path flagPath = new Path();
        flagPath.moveTo(centerX - size * 0.2f, centerY - size * 0.3f);
        flagPath.lineTo(centerX + size * 0.3f, centerY - size * 0.1f);
        flagPath.lineTo(centerX - size * 0.2f, centerY + 0.1f * size);
        flagPath.close();
        canvas.drawPath(flagPath, cellPaint);
    }

    private void drawMine(Canvas canvas, RectF rect) {
        float centerX = rect.centerX();
        float centerY = rect.centerY();
        float radius = Math.min(rect.width(), rect.height()) * 0.25f;

        // Draw mine body
        cellPaint.setColor(getColorMine());
        cellPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, radius, cellPaint);

        // Draw spikes
        cellPaint.setStrokeWidth(radius * 0.3f);
        cellPaint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2 * i / 8;
            float x1 = centerX + (float) Math.cos(angle) * radius * 0.7f;
            float y1 = centerY + (float) Math.sin(angle) * radius * 0.7f;
            float x2 = centerX + (float) Math.cos(angle) * radius * 1.5f;
            float y2 = centerY + (float) Math.sin(angle) * radius * 1.5f;
            canvas.drawLine(x1, y1, x2, y2, cellPaint);
        }

        // Draw highlight
        cellPaint.setColor(0x80FFFFFF);
        cellPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX - radius * 0.3f, centerY - radius * 0.3f, radius * 0.3f, cellPaint);
    }

    private void drawNumber(Canvas canvas, RectF rect, int number) {
        textPaint.setColor(NUMBER_COLORS[number]);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float textY = rect.centerY() - (metrics.descent + metrics.ascent) / 2;
        canvas.drawText(String.valueOf(number), rect.centerX(), textY, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            handleTouch(event.getX(), event.getY());
            return true;
        }
        return true;
    }

    private void handleTouch(float x, float y) {
        if (engine == null || listener == null)
            return;

        // Convert touch coordinates to grid coordinates
        int c = (int) ((x - offsetX) / cellSize);
        int r = (int) ((y - offsetY) / cellSize);

        if (r >= 0 && r < engine.getRows() && c >= 0 && c < engine.getCols()) {
            listener.onCellClick(r, c);
        }
    }
}
