package com.ensao.mytime.alarm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;

/**
 * Custom analog clock view that displays the current time with hour, minute,
 * and second hands.
 */
public class AnalogClockView extends View {

    private Paint circlePaint;
    private Paint tickPaint;
    private Paint hourHandPaint;
    private Paint minuteHandPaint;
    private Paint secondHandPaint;
    private Paint centerDotPaint;

    private int centerX;
    private int centerY;
    private int radius;

    private Handler handler;
    private Runnable updateRunnable;
    private boolean isRunning = false;

    public AnalogClockView(Context context) {
        super(context);
        init();
    }

    public AnalogClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnalogClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Circle outline paint
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.WHITE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(6f);

        // Tick marks paint
        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(Color.WHITE);
        tickPaint.setStrokeWidth(4f);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);

        // Hour hand paint
        hourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourHandPaint.setColor(Color.WHITE);
        hourHandPaint.setStrokeWidth(10f);
        hourHandPaint.setStrokeCap(Paint.Cap.ROUND);

        // Minute hand paint
        minuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        minuteHandPaint.setColor(Color.WHITE);
        minuteHandPaint.setStrokeWidth(6f);
        minuteHandPaint.setStrokeCap(Paint.Cap.ROUND);

        // Second hand paint
        secondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondHandPaint.setColor(0xFFFF5252); // Red accent color
        secondHandPaint.setStrokeWidth(3f);
        secondHandPaint.setStrokeCap(Paint.Cap.ROUND);

        // Center dot paint
        centerDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerDotPaint.setColor(0xFFFF5252);
        centerDotPaint.setStyle(Paint.Style.FILL);

        handler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
                if (isRunning) {
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = h / 2;
        radius = Math.min(centerX, centerY) - 20;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (radius <= 0)
            return;

        // Draw clock circle
        canvas.drawCircle(centerX, centerY, radius, circlePaint);

        // Draw tick marks
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            float innerRadius = (i % 3 == 0) ? radius - 30 : radius - 20;
            float outerRadius = radius - 10;

            float startX = centerX + (float) (innerRadius * Math.cos(angle));
            float startY = centerY + (float) (innerRadius * Math.sin(angle));
            float endX = centerX + (float) (outerRadius * Math.cos(angle));
            float endY = centerY + (float) (outerRadius * Math.sin(angle));

            tickPaint.setStrokeWidth((i % 3 == 0) ? 6f : 3f);
            canvas.drawLine(startX, startY, endX, endY, tickPaint);
        }

        // Get current time
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        // Draw hour hand
        float hourAngle = (float) Math.toRadians((hours + minutes / 60f) * 30 - 90);
        float hourHandLength = radius * 0.5f;
        float hourX = centerX + (float) (hourHandLength * Math.cos(hourAngle));
        float hourY = centerY + (float) (hourHandLength * Math.sin(hourAngle));
        canvas.drawLine(centerX, centerY, hourX, hourY, hourHandPaint);

        // Draw minute hand
        float minuteAngle = (float) Math.toRadians((minutes + seconds / 60f) * 6 - 90);
        float minuteHandLength = radius * 0.7f;
        float minuteX = centerX + (float) (minuteHandLength * Math.cos(minuteAngle));
        float minuteY = centerY + (float) (minuteHandLength * Math.sin(minuteAngle));
        canvas.drawLine(centerX, centerY, minuteX, minuteY, minuteHandPaint);

        // Draw second hand
        float secondAngle = (float) Math.toRadians(seconds * 6 - 90);
        float secondHandLength = radius * 0.8f;
        float secondX = centerX + (float) (secondHandLength * Math.cos(secondAngle));
        float secondY = centerY + (float) (secondHandLength * Math.sin(secondAngle));
        canvas.drawLine(centerX, centerY, secondX, secondY, secondHandPaint);

        // Draw center dot
        canvas.drawCircle(centerX, centerY, 12, centerDotPaint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startClock();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopClock();
    }

    public void startClock() {
        if (!isRunning) {
            isRunning = true;
            handler.post(updateRunnable);
        }
    }

    public void stopClock() {
        isRunning = false;
        handler.removeCallbacks(updateRunnable);
    }
}
