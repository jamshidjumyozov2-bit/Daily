package com.example.haftalikharajat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.Locale;

public class LineChartView extends View {
    public interface OnPointClickListener {
        void onPointClick(int index);
    }

    private double[] data = new double[0];
    private float maxValue = 300000f;
    private String labelPrefix = "";
    private OnPointClickListener listener;

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path path = new Path();
    private final Path fillPath = new Path();
    private LinearGradient gradient;
    
    private float[] pointXs;
    private float[] pointYs;
    private float density;

    private float paddingLeft = 50f;
    private float paddingRight = 30f;
    private float paddingTop = 40f;
    private float paddingBottom = 30f;
    private boolean isMiniMode = false;

    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;
        
        paddingLeft *= density;
        paddingRight *= density;
        paddingTop *= density;
        paddingBottom *= density;

        linePaint.setColor(0xFF3B82F6);
        linePaint.setStrokeWidth(3 * density);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        pointPaint.setColor(0xFF3B82F6);
        pointPaint.setStyle(Paint.Style.FILL);

        pointStrokePaint.setColor(0xFF0F172A);
        pointStrokePaint.setStrokeWidth(2 * density);
        pointStrokePaint.setStyle(Paint.Style.STROKE);

        fillPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(9 * density);
        textPaint.setTextAlign(Paint.Align.CENTER);

        axisLabelPaint.setColor(0xFF94A3B8);
        axisLabelPaint.setTextSize(9 * density);
        
        gridPaint.setColor(0x0DFFFFFF); // Very subtle grid
        gridPaint.setStrokeWidth(1 * density);
    }

    public void setData(double[] values, double max, String prefix) {
        this.data = values;
        this.maxValue = (float) max;
        this.labelPrefix = prefix;
        if (this.maxValue <= 0) this.maxValue = 300000f;
        
        if (data != null) {
            pointXs = new float[data.length];
            pointYs = new float[data.length];
        } else {
            pointXs = null;
            pointYs = null;
        }
        
        invalidate();
    }

    public void setOnPointClickListener(OnPointClickListener listener) {
        this.listener = listener;
    }

    public void setMiniMode(boolean miniMode) {
        this.isMiniMode = miniMode;
        if (miniMode) {
            paddingLeft = 0;
            paddingRight = 0;
            paddingTop = 10 * density;
            paddingBottom = 5 * density;
            linePaint.setStrokeWidth(2 * density);
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h > 0) {
            gradient = new LinearGradient(0, paddingTop, 0, h - paddingBottom, 0x663B82F6, 0x003B82F6, Shader.TileMode.CLAMP);
            fillPaint.setShader(gradient);
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (pointXs == null || pointYs == null) return false;
            
            float tx = event.getX();
            float ty = event.getY();
            
            int closestIndex = -1;
            float minDistance = 40 * density;
            
            for (int i = 0; i < pointXs.length; i++) {
                float dx = pointXs[i] - tx;
                float dy = pointYs[i] - ty;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < minDistance) {
                    minDistance = dist;
                    closestIndex = i;
                }
            }
            
            if (closestIndex != -1 && listener != null) {
                performClick();
                listener.onPointClick(closestIndex);
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null || data.length == 0 || pointXs == null || pointYs == null) return;

        float w = getWidth();
        float h = getHeight();

        if (isMiniMode) {
            drawMiniChart(canvas, w, h);
            return;
        }
        
        // Draw Y-Axis Labels and Grid Lines
        int gridCount = 6;
        axisLabelPaint.setTextAlign(Paint.Align.RIGHT);
        
        // Dynamic padding calculation for Y-axis labels
        float maxLabelWidth = 0;
        for (int i = 0; i <= gridCount; i++) {
            float labelVal = (maxValue * i / gridCount);
            String label = formatValueSimple(labelVal);
            maxLabelWidth = Math.max(maxLabelWidth, axisLabelPaint.measureText(label));
        }
        paddingLeft = maxLabelWidth + 12 * density;

        float usableW = w - paddingLeft - paddingRight;
        float usableH = h - paddingTop - paddingBottom;
        float stepX = data.length > 1 ? usableW / (data.length - 1) : usableW;

        for (int i = 0; i <= gridCount; i++) {
            float y = paddingTop + usableH * (gridCount - i) / gridCount;
            canvas.drawLine(paddingLeft, y, w - paddingRight, y, gridPaint);
            
            float labelVal = (maxValue * i / gridCount);
            canvas.drawText(formatValueSimple(labelVal), paddingLeft - 8 * density, y + 4 * density, axisLabelPaint);
        }

        path.reset();
        fillPath.reset();

        axisLabelPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < data.length; i++) {
            float x = paddingLeft + i * stepX;
            float valRatio = (maxValue > 0) ? (float) (data[i] / maxValue) : 0f;
            if (valRatio > 1f) valRatio = 1f;
            float y = (paddingTop + usableH) - (valRatio * usableH);

            pointXs[i] = x;
            pointYs[i] = y;

            if (i == 0) {
                path.moveTo(x, y);
                fillPath.moveTo(x, paddingTop + usableH);
                fillPath.lineTo(x, y);
            } else {
                // Bezier curve for smoother lines
                float prevX = pointXs[i - 1];
                float prevY = pointYs[i - 1];
                path.cubicTo((prevX + x) / 2, prevY, (prevX + x) / 2, y, x, y);
                fillPath.cubicTo((prevX + x) / 2, prevY, (prevX + x) / 2, y, x, y);
            }

            if (i == data.length - 1) {
                fillPath.lineTo(x, paddingTop + usableH);
                fillPath.close();
            }
            
            // X-axis label
            String xLabel = labelPrefix.isEmpty() ? String.valueOf(i + 1) : (i + 1) + labelPrefix;
            if (labelPrefix.equals("-hafta")) xLabel = (i + 1) + "-hafta";
            
            if (data.length > 15) {
                if (i == 0 || i == 4 || i == 9 || i == 14 || i == 19 || i == 24 || i == 29 || i == data.length - 1) {
                    canvas.drawText(xLabel, x, h - 8 * density, axisLabelPaint);
                }
            } else {
                canvas.drawText(xLabel, x, h - 8 * density, axisLabelPaint);
            }
        }

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(path, linePaint);

        for (int i = 0; i < data.length; i++) {
            float px = pointXs[i];
            float py = pointYs[i];
            
            canvas.drawCircle(px, py, 4 * density, pointPaint);
            canvas.drawCircle(px, py, 4 * density, pointStrokePaint);

            if (data[i] > 0) {
                String label;
                if (data[i] >= 1000000) label = String.format(Locale.US, "%.1fM", data[i]/1000000.0);
                else if (data[i] >= 1000) label = String.format(Locale.US, "%.0fK", data[i]/1000.0);
                else label = String.valueOf((int)data[i]);
                
                // Only draw data labels if not too crowded
                if (data.length <= 15 || i % 2 == 0) {
                    canvas.drawText(label, px, py - 12 * density, textPaint);
                }
            }
        }
    }

    private String formatValue(float val) {
        if (val >= 1000000) return String.format(Locale.US, "%,.1fM", val / 1000000.0).replace(".0", "");
        else if (val >= 1000) return String.format(Locale.US, "%,.0f", val).replace(",", " ");
        else return String.valueOf((int) val);
    }

    private String formatValueSimple(float val) {
        if (val >= 1000) return String.format(Locale.US, "%.0f", val).replace(",", " ");
        return String.valueOf((int) val);
    }

    private void drawMiniChart(Canvas canvas, float w, float h) {
        float usableW = w - paddingLeft - paddingRight;
        float usableH = h - paddingTop - paddingBottom;
        float stepX = data.length > 1 ? usableW / (data.length - 1) : usableW;

        path.reset();
        fillPath.reset();

        for (int i = 0; i < data.length; i++) {
            float x = paddingLeft + i * stepX;
            float valRatio = (maxValue > 0) ? (float) (data[i] / maxValue) : 0f;
            if (valRatio > 1f) valRatio = 1f;
            float y = (paddingTop + usableH) - (valRatio * usableH);

            pointXs[i] = x;
            pointYs[i] = y;

            if (i == 0) {
                path.moveTo(x, y);
                fillPath.moveTo(x, paddingTop + usableH);
                fillPath.lineTo(x, y);
            } else {
                float prevX = pointXs[i - 1];
                float prevY = pointYs[i - 1];
                path.cubicTo((prevX + x) / 2, prevY, (prevX + x) / 2, y, x, y);
                fillPath.cubicTo((prevX + x) / 2, prevY, (prevX + x) / 2, y, x, y);
            }

            if (i == data.length - 1) {
                fillPath.lineTo(x, paddingTop + usableH);
                fillPath.close();
            }
        }

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(path, linePaint);
    }
}
