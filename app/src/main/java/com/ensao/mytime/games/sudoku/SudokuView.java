package com.ensao.mytime.games.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.ensao.mytime.R;

import java.util.Set;

public class SudokuView extends View {

    private SudokuBoard board;
    private Paint linePaint;
    private Paint thickLinePaint;
    private Paint numberPaint;
    private Paint cluePaint;
    private Paint selectedPaint;
    private Paint selectedStrokePaint;
    private Paint errorPaint;
    private Paint notePaint;
    private Paint backgroundPaint;

    private float cellSize;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private OnCellSelectedListener cellSelectedListener;

    public interface OnCellSelectedListener {
        void onCellSelected(int row, int col);
    }

    public SudokuView(Context context) {
        super(context);
        init(context);
    }

    public SudokuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // Background
        backgroundPaint = new Paint();
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.grid_background));

        // Thin grid lines
        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(context, R.color.grid_line));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f);

        // Thick grid lines (3x3 boxes)
        thickLinePaint = new Paint();
        thickLinePaint.setColor(ContextCompat.getColor(context, R.color.grid_line_thick));
        thickLinePaint.setStyle(Paint.Style.STROKE);
        thickLinePaint.setStrokeWidth(6f);

        // User input numbers
        numberPaint = new Paint();
        numberPaint.setColor(ContextCompat.getColor(context, R.color.number_user));
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setAntiAlias(true);
        numberPaint.setTextSize(60f);

        // Clue numbers (initial puzzle)
        cluePaint = new Paint();
        cluePaint.setColor(ContextCompat.getColor(context, R.color.number_clue));
        cluePaint.setTextAlign(Paint.Align.CENTER);
        cluePaint.setAntiAlias(true);
        cluePaint.setTextSize(60f);
        cluePaint.setFakeBoldText(true);

        // Selected cell highlight
        selectedPaint = new Paint();
        selectedPaint.setColor(ContextCompat.getColor(context, R.color.cell_selected));
        selectedPaint.setStyle(Paint.Style.FILL);

        // Selected cell border
        selectedStrokePaint = new Paint();
        selectedStrokePaint.setColor(ContextCompat.getColor(context, R.color.cell_selected_border));
        selectedStrokePaint.setStyle(Paint.Style.STROKE);
        selectedStrokePaint.setStrokeWidth(5f);

        // Error highlight
        errorPaint = new Paint();
        errorPaint.setColor(ContextCompat.getColor(context, R.color.cell_error));
        errorPaint.setStyle(Paint.Style.FILL);

        // Notes/candidates
        notePaint = new Paint();
        notePaint.setColor(ContextCompat.getColor(context, R.color.note_text));
        notePaint.setTextAlign(Paint.Align.CENTER);
        notePaint.setAntiAlias(true);
        notePaint.setTextSize(20f);
    }

    public void setBoard(SudokuBoard board) {
        this.board = board;
        invalidate();
    }

    public void setOnCellSelectedListener(OnCellSelectedListener listener) {
        this.cellSelectedListener = listener;
    }

    public void setSelectedCell(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        invalidate();
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = w / 9f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (board == null)
            return;

        // Draw background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        // Draw selected cell and related cells
        drawSelectedHighlight(canvas);

        // Draw error cells
        drawErrors(canvas);

        // Draw grid lines
        drawGrid(canvas);

        // Draw numbers
        drawNumbers(canvas);
    }

    private void drawSelectedHighlight(Canvas canvas) {
        if (selectedRow >= 0 && selectedCol >= 0) {
            // Highlight selected cell with border
            RectF rect = new RectF(
                    selectedCol * cellSize,
                    selectedRow * cellSize,
                    (selectedCol + 1) * cellSize,
                    (selectedRow + 1) * cellSize);
            canvas.drawRoundRect(rect, 8f, 8f, selectedPaint);
            canvas.drawRoundRect(rect, 8f, 8f, selectedStrokePaint);
        }
    }

    private void drawErrors(Canvas canvas) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board.hasError(i, j)) {
                    RectF rect = new RectF(
                            j * cellSize,
                            i * cellSize,
                            (j + 1) * cellSize,
                            (i + 1) * cellSize);
                    canvas.drawRoundRect(rect, 8f, 8f, errorPaint);
                }
            }
        }
    }

    private void drawGrid(Canvas canvas) {
        // Draw thin lines
        for (int i = 0; i <= 9; i++) {
            // Vertical lines
            canvas.drawLine(i * cellSize, 0, i * cellSize, getHeight(), linePaint);
            // Horizontal lines
            canvas.drawLine(0, i * cellSize, getWidth(), i * cellSize, linePaint);
        }

        // Draw thick lines for 3x3 boxes
        for (int i = 0; i <= 9; i += 3) {
            // Vertical lines
            canvas.drawLine(i * cellSize, 0, i * cellSize, getHeight(), thickLinePaint);
            // Horizontal lines
            canvas.drawLine(0, i * cellSize, getWidth(), i * cellSize, thickLinePaint);
        }
    }

    private void drawNumbers(Canvas canvas) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int value = board.getValue(i, j);

                if (value != 0) {
                    // Draw number
                    Paint paint = board.isClue(i, j) ? cluePaint : numberPaint;

                    float x = j * cellSize + cellSize / 2;
                    float y = i * cellSize + cellSize / 2 - (paint.descent() + paint.ascent()) / 2;

                    canvas.drawText(String.valueOf(value), x, y, paint);
                } else {
                    // Draw notes if any
                    Set<String> notes = board.getNotes(i, j);
                    if (!notes.isEmpty()) {
                        drawNotes(canvas, i, j, notes);
                    }
                }
            }
        }
    }

    private void drawNotes(Canvas canvas, int row, int col, Set<String> notes) {
        float baseX = col * cellSize;
        float baseY = row * cellSize;
        float miniCellSize = cellSize / 3;

        for (String note : notes) {
            int num = Integer.parseInt(note);
            int noteRow = (num - 1) / 3;
            int noteCol = (num - 1) % 3;

            float x = baseX + noteCol * miniCellSize + miniCellSize / 2;
            float y = baseY + noteRow * miniCellSize + miniCellSize / 2 -
                    (notePaint.descent() + notePaint.ascent()) / 2;

            canvas.drawText(note, x, y, notePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            int col = (int) (x / cellSize);
            int row = (int) (y / cellSize);

            if (row >= 0 && row < 9 && col >= 0 && col < 9) {
                selectedRow = row;
                selectedCol = col;

                if (cellSelectedListener != null) {
                    cellSelectedListener.onCellSelected(row, col);
                }

                invalidate();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }
}
