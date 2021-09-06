package com.example.grad;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;


public class ResultVideoRenderer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_result_video_view);
        Intent received_intent = getIntent();
        final Bundle bundle = received_intent.getExtras();
        ArrayList<float[]> points = new ArrayList<>();
        if (bundle.getBoolean("flag") == true) {
            for (int i = 0; i < 33; i++) {
                float[] xy = bundle.getFloatArray("pose" + i);
                points.add(xy);
//                System.out.println(xy[0] + ", " + xy[1]);
            }
        }
        Uri uri = bundle.getParcelable("uri");
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        final Paint mPaint = new Paint();
//        mPaint.setStrokeWidth((float) 20);
//        mPaint.setColor(Color.RED);
//        final Bitmap finalBitmap = bitmap;
//        setContentView(new View(this) {
//            @Override
//            protected void onDraw(Canvas canvas) {
//                canvas.drawBitmap(finalBitmap, 0, 0, mPaint);
//                if (bundle.getBoolean("flag") == true) {
//                    for (int i = 0; i < 33; i++) {
//                        float[] xy = bundle.getFloatArray("pose"+i);
//                        canvas.drawPoint(xy[0], xy[1], mPaint);
//                    }
//                }
//            }
//        });
        setContentView(new ResultVideoView(this, points, bitmap));
    }
}