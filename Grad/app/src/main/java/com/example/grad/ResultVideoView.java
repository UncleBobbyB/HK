package com.example.grad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;

public class ResultVideoView extends View {
    private ArrayList<float[]> points;
    private Bitmap bitmap;
    private Paint mPaint;

    public ResultVideoView(Context context) {
        super(context);
    }

    public ResultVideoView(ResultVideoRenderer resultVideoRenderer, ArrayList<float[]> points, Bitmap bitmap) {
        super(resultVideoRenderer);
        mPaint = new Paint();
        this.points = points;
        this.bitmap = bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, mPaint);
        mPaint.setStrokeWidth((float) 20);
        mPaint.setColor(Color.RED);
        assert (points.size() == 33);
        for (int i = 0; i < points.size(); i++) {
            canvas.drawPoint(-points.get(i)[1], points.get(i)[0], mPaint);
        }
    }
}
