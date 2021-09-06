package com.example.grad;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Queue;

public class Predictor {
    TensorFlowInferenceInterface inferenceInterface;
    private String INPUT_NAME;
    private String OUTPUT_NAME;

    static {
        System.loadLibrary("tensorflow_inference");
        Log.e("tensorflow","libtensorflow_inference.so loaded");
    }

    Predictor(AssetManager assetManager, String modePath) {
        inferenceInterface = new TensorFlowInferenceInterface(assetManager,modePath);
        Log.e("tf","TensoFlow model file loaded");
        INPUT_NAME = "input_1";
        OUTPUT_NAME = "output_1";
    }

    private float[] shapeInput(Queue<Point[]> pose_collections) {
        float[] input = new float[10*2*33];
        int cur = 0;
        for (Point[] points : pose_collections) {
            for (int i = 0; i < 33; i++) {
                input[cur++] = points[i].getX();
                input[cur++] = points[i].getY();
            }
        }
        assert (cur == 659);
        return input;
    }

    public float[] predict(Queue<Point[]> pose_collections) {
        float[] input = shapeInput(pose_collections);
        float[] output = new float[2*33];
        inferenceInterface.feed(INPUT_NAME, input, 1, 10, 2*33);
        inferenceInterface.run(new String[] {OUTPUT_NAME});
        inferenceInterface.fetch(OUTPUT_NAME, output);
        return output;
    }
}
