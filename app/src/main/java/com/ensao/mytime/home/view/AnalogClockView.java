package com.ensao.mytime.home.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import java.util.Calendar;

// Vue personnalisée pour dessiner une horloge analogique en temps réel avec chiffres
public class AnalogClockView extends View {

    private Paint mPaintTick, mPaintHand, mPaintText;
    private float mCenterX, mCenterY, mRadius;
    private Handler mHandler;
    private Runnable mTicker;

    public AnalogClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Peinture pour les marques de l'horloge
        mPaintTick = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTick.setColor(Color.parseColor("#999999")); // Gris doux
        mPaintTick.setStrokeWidth(3f);
        mPaintTick.setStyle(Paint.Style.STROKE);

        // Peinture pour les aiguilles (Couleur or/laiton)
        mPaintHand = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintHand.setColor(Color.parseColor("#B8860B")); // Gold/DarkYellow
        mPaintHand.setStyle(Paint.Style.STROKE);
        mPaintHand.setStrokeCap(Paint.Cap.ROUND);

        // Peinture pour les chiffres horaires
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setColor(Color.parseColor("#1C1C1C"));
        mPaintText.setTextSize(mRadius / 10); // Taille du texte ajustée dynamiquement
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2f;
        mCenterY = h / 2f;
        mRadius = Math.min(w, h) / 2f * 0.9f;
        // Mettre à jour la taille du texte après avoir calculé le rayon
        mPaintText.setTextSize(mRadius / 8);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. Dessiner le disque de fond (cercle plein)
        mPaintHand.setStyle(Paint.Style.FILL); // Important : Dessiner l'intérieur
        mPaintHand.setColor(Color.parseColor("#CBCED3"));
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaintHand);

        // 2. Dessiner le cercle extérieur (cadran) - L'ancienne ligne
        mPaintHand.setStyle(Paint.Style.STROKE);
        mPaintHand.setStrokeWidth(2f);
        mPaintHand.setColor(Color.parseColor("#CBCED3")); // Couleur du contour
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaintHand);

        // Dessiner les marques horaires et les chiffres
        for (int i = 1; i <= 12; i++) {
            float angle = (float) (Math.PI / 6 * (i - 3)); // Angle en radians, ajusté pour que 12 soit en haut

            // Calculer la position pour les marques
            float startX = (float) (mCenterX + (mRadius * 0.9f) * Math.cos(angle));
            float startY = (float) (mCenterY + (mRadius * 0.9f) * Math.sin(angle));
            float stopX = (float) (mCenterX + (mRadius * 0.8f) * Math.cos(angle));
            float stopY = (float) (mCenterY + (mRadius * 0.8f) * Math.sin(angle));

            // Dessiner les marques (petites lignes)
            mPaintTick.setStrokeWidth(i % 3 == 0 ? 5f : 3f);
            canvas.drawLine(startX, startY, stopX, stopY, mPaintTick);

            // Calculer la position pour les chiffres (légèrement plus près du centre)
            float textRadius = mRadius * 0.7f; // Position des chiffres
            float textX = (float) (mCenterX + textRadius * Math.cos(angle));
            float textY = (float) (mCenterY + textRadius * Math.sin(angle));

            // Ajustement pour centrer le texte verticalement
            float textOffset = (mPaintText.descent() + mPaintText.ascent()) / 2 ;

            // Dessiner le chiffre
            canvas.drawText(String.valueOf(i), textX, textY - textOffset, mPaintText);
        }

        // Obtenir l'heure actuelle
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        // Calculer les angles des aiguilles (en degrés)
        float secAngle = second * 6f;
        float minAngle = minute * 6f + (second / 60f) * 6f;
        float hourAngle = hour * 30f + (minute / 60f) * 30f;

        // Dessiner l'aiguille des heures (courte et épaisse)
        drawHand(canvas, hourAngle, mRadius * 0.5f, 10f, Color.parseColor("#B8860B"));

        // Dessiner l'aiguille des minutes (longue et moyenne)
        drawHand(canvas, minAngle, mRadius * 0.75f, 6f, Color.parseColor("#B8860B"));

        // Dessiner l'aiguille des secondes (très fine)
        drawHand(canvas, secAngle, mRadius * 0.8f, 2f, Color.parseColor("#708090"));

        // Dessiner le point central
        mPaintHand.setStyle(Paint.Style.FILL);
        mPaintHand.setColor(Color.parseColor("#1C1C1C"));
        canvas.drawCircle(mCenterX, mCenterY, 15f, mPaintHand);
    }

    // Fonction utilitaire pour dessiner une aiguille
    private void drawHand(Canvas canvas, float degrees, float length, float strokeWidth, int color) {
        float angle = (float) (Math.PI * degrees / 180.0f); // Convertir en radians
        mPaintHand.setColor(color);
        mPaintHand.setStrokeWidth(strokeWidth);
        canvas.drawLine(mCenterX, mCenterY,
                (float) (mCenterX + length * Math.sin(angle)),
                (float) (mCenterY - length * Math.cos(angle)),
                mPaintHand);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler = new Handler();
        // Création du Runnable pour l'animation de l'horloge
        mTicker = new Runnable() {
            public void run() {
                invalidate();
                long now = android.os.SystemClock.uptimeMillis();
                long next = now + (1000 - now % 1000);
                mHandler.postAtTime(mTicker, next);
            }
        };
        mTicker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mHandler != null) {
            mHandler.removeCallbacks(mTicker);
        }
    }
}