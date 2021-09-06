package com.example.grad;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    int counter = 0;
    int width = 480;
    int height = 360;
    Mat frame;
    PoseDetectorOptions options;
    PoseDetector poseDetector;
    Queue<Point[]> pose_collections;
    Predictor predictor;
    float drawing_threshold = (float) 0.05;

    final int[][] connections = {{18, 20}, {20, 22}, {18, 16}, {16, 22}, {16, 14}, {14, 12}, {12, 11},
            {11, 13}, {13, 15}, {21, 15}, {15, 17}, {15, 19}, {17, 19}, {12, 24}, {24, 26}, {26, 28},
            {28, 32}, {28, 30}, {30, 32}, {11, 23}, {24, 23}, {23, 25}, {25, 27}, {27, 29}, {29, 31},
            {27, 31}, {8, 6}, {5, 6}, {5, 4}, {4, 0}, {0, 1}, {1, 2}, {2, 3}, {3, 7}, {9, 10}}; //35 in total
    Map<Integer, ArrayList<Integer> > map;

    private Map<Integer, ArrayList<Integer> > getMap() {
        Map<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer> >();
        for (int i = 0; i < 33; i++) {
            map.put(i, new ArrayList<Integer>());
        }
        for (int i = 0; i < 35; i++) {
            map.get(connections[i][0]).add(connections[i][1]);
        }
        return map;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AssetManager assetManager = getAssets();
        predictor = new Predictor(assetManager, "model.pb");

        boolean havePermission = getPermissionCamera(this);

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        cameraBridgeViewBase.setMaxFrameSize(480, 360);
        cameraBridgeViewBase.setCvCameraViewListener(this);


        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

        pose_collections = new LinkedList<Point[]>();
        for (int i = 0; i < 10; i++) {
            Point[] new_points = new Point[33];
            for (int j = 0; j < 33; j++) {
                new_points[j] = new Point(0, 0);
            }
            pose_collections.offer(new_points);
        }

        options = new PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                        .build();
        poseDetector = PoseDetection.getClient(options);

        map = getMap();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        frame = inputFrame.rgba();
        counter++;

        Bitmap frame_bitmap = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, frame_bitmap, true);

        getPoseFromFrame(frame_bitmap);
        float[] predicted = predictor.predict(pose_collections);
        renderFrame(predicted);
        return frame;
    }

    private void drawConnections(Pose pose) {
        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        for (int i = 0; i < landmarks.size(); i++ ){
            for (int j : map.get(landmarks.get(i).getLandmarkType())) {
                Imgproc.line(frame, new org.opencv.core.Point(landmarks.get(i).getPosition().x,
                                landmarks.get(i).getPosition().y),
                        new org.opencv.core.Point(landmarks.get(j).getPosition().x,
                                landmarks.get(j).getPosition().y),
                        new Scalar(255.0, 0, 0), 1);
//                Imgproc.line(frame, new org.opencv.core.Point(50, 50),
//                        new org.opencv.core.Point(200, 200), new Scalar(0, 255.0, 0), 3);
            }
        }
    }

    private void drawConnections(float[] coords) {
        for (int i = 0; i < 33; i++) {
            if (coords[2*i] <= drawing_threshold || coords[2*i+1] <= drawing_threshold) {
                continue;
            }
            for (int j : map.get(i)) {
                if (coords[2*j] <= drawing_threshold || coords[2*j+1] <= drawing_threshold) {
                    continue;
                }
                org.opencv.core.Point p1 = new org.opencv.core.Point(width*coords[2*i], height*coords[2*i+1]);
                org.opencv.core.Point p2 = new org.opencv.core.Point(width*coords[2*j], height*coords[2*j+1]);
                Log.e("Point1", String.valueOf(p1.x)+", "+String.valueOf(p1.y));
                Log.e("Point2", String.valueOf(p2.x)+", "+String.valueOf(p2.y));
                Imgproc.line(frame, p1, p2, new Scalar(0, 255.0, 0), 1);
            }
        }
    }

    private void renderFrame(float[] coords) {
        for (int i = 0; i < 33; i++) {
            Imgproc.drawMarker(frame, new org.opencv.core.Point(width*coords[2*i], height*coords[2*i+1]),
                    new Scalar(0, 255.0, 0), Imgproc.MARKER_TRIANGLE_UP, 5);
        }
//        drawConnections(coords);
    }

    private void renderFrame(Pose pose) {
        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        for (PoseLandmark landmark : landmarks) {
            float x = landmark.getPosition().x;
            float y = landmark.getPosition().y;
            Imgproc.drawMarker(frame, new org.opencv.core.Point(x, y), new Scalar(255.0, 0, 0),
                    Imgproc.MARKER_STAR, 5);
        }
        drawConnections(pose);
//        Imgproc.putText(frame, String.valueOf(counter), new org.opencv.core.Point(50, 300),
//                Imgproc.FONT_HERSHEY_COMPLEX, 2, new Scalar(255, 255, 0), 2);
    }


    private void getPoseFromFrame(Bitmap frame_bitmap) {
        InputImage image = InputImage.fromBitmap(frame_bitmap, 0);
        Task<Pose> result = poseDetector.process(image);
        while (!result.isComplete()) {
            continue;
        }
        final Pose pose = result.getResult();
        int i = 0;
        Point[] new_points = new Point[33];
        for (PoseLandmark landmark : pose.getAllPoseLandmarks()) {
            float x = landmark.getPosition().x;
            float y = landmark.getPosition().y;
            new_points[i++] = new Point(x, y);
        }
        if (i == 0) {
            for (int j = 0; j < 33; j++) {
                new_points[j] = new Point(0, 0);
            }
            pose_collections.poll();
            pose_collections.offer(new_points);
            return ;
        } else {
            renderFrame(pose);
            for (int j = 0; j < 33; j++) {
                new_points[j].setX(new_points[j].getX() / width);
                new_points[j].setY(new_points[j].getY() / height);
            }
            pose_collections.poll();
            pose_collections.offer(new_points);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There's a problem", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    public static boolean getPermissionCamera(Activity activity) {
        int cameraPermissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int readPermissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED
                || readPermissionCheck != PackageManager.PERMISSION_GRANTED
                || writePermissionCheck != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    0);
            return false;
        } else {
            return true;
        }
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, boolean isFrontFacing)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // Get the device's sensor orientation.
        CameraManager cameraManager = (CameraManager) activity.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);

        if (isFrontFacing) {
            rotationCompensation = (sensorOrientation + rotationCompensation) % 360;
        } else { // back-facing
            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360;
        }
        return rotationCompensation;
    }
}