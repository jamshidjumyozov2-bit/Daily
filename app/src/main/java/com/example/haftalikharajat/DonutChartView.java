package com.example.haftalikharajat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DonutChartView extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<CategorySummary> data = new ArrayList<>();
    private String centerText = "";
    private float strokeWidth = 30f;

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setData(List<CategorySummary> data, String centerText) {
        this.data = data;
        this.centerText = centerText;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float radius = (Math.min(width, height) / 2) - strokeWidth;
        RectF rect = new RectF(width / 2 - radius, height / 2 - radius, width / 2 + radius, height / 2 + radius);

        paint.setStrokeWidth(strokeWidth);
        
        if (data == null || data.isEmpty()) {
            paint.setColor(0xFF1E293B);
            canvas.drawCircle(width / 2, height / 2, radius, paint);
        } else {
            float total = 0;
            for (CategorySummary item : data) total += item.amount;
            
            if (total == 0) {
                paint.setColor(0xFF1E293B);
                canvas.drawCircle(width / 2, height / 2, radius, paint);
            } else {
                float startAngle = -90f;
                for (CategorySummary item : data) {
                    float sweepAngle = (float) (item.amount / total * 360f);
                    // Ensure small slices are visible
                    if (sweepAngle < 2f && item.amount > 0) sweepAngle = 2f;
                    
                    paint.setColor(item.color);
                    canvas.drawArc(rect, startAngle, sweepAngle, false, paint);
                    startAngle += sweepAngle;
                }
            }
        }

        // Center Text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(36f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        canvas.drawText(centerText, width / 2, height / 2 + 12f, textPaint);
        
        textPaint.setTextSize(24f);
        textPaint.setFakeBoldText(false);
        textPaint.setColor(0xFF94A3B8);
        canvas.drawText("so'm", width / 2, height / 2 + 45f, textPaint);
    }
}
