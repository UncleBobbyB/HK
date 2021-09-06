package com.example.grad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class Test extends AppCompatActivity {
    private static final int REQUEST_PHOTO_GALLERY = 1;
    private static final int REQUEST_VIDEO_DISPLAY = 2;

    private ImageView iv_image;

    public void gallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PHOTO_GALLERY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PHOTO_GALLERY) {
            if (data != null) {
                Uri uri = data.getData();
                initPoseDetector(uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        this.iv_image = (ImageView) this.findViewById(R.id.iv_image);
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_PERMISSION_STORAGE = 100;
            String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    this.requestPermissions(permissions, REQUEST_CODE_PERMISSION_STORAGE);
                    return;
                }
            }
        }

    }

    private void initPoseDetector(final Uri uri) {
        PoseDetectorOptions options =
                new PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                        .build();
        PoseDetector poseDetector = PoseDetection.getClient(options);
        FileInputStream fis = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "Pictures/a.jpeg");
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap  = BitmapFactory.decodeStream(fis);
        InputImage image = InputImage.fromBitmap(bitmap, 270);
//        InputImage image = null;
//        try {
//            image = InputImage.fromFilePath(this, uri);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        final Task<Pose> result =
                poseDetector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<Pose>() {
                                    @Override
                                    public void onSuccess(Pose pose) {
                                        Intent intent = new Intent(
                                                Test.this, ResultVideoRenderer.class);
                                        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
                                        if (landmarks.size() == 0) {
                                            intent.putExtra("flag", false);
                                        } else {
                                            Bundle bundle = new Bundle();
                                            bundle.putBoolean("flag", true);
                                            assert(landmarks.size() == 33);
                                            for (int i = 0; i < landmarks.size(); i++) {
                                                bundle.putFloatArray("pose"+i,
                                                        new float[]{landmarks.get(i).getPosition().x,
                                                        landmarks.get(i).getPosition().y});
                                            }
                                            bundle.putParcelable("uri", uri);
                                            intent.putExtras(bundle);
                                        }
                                        startActivity(intent);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println("Failed");
                                        e.printStackTrace();
                                    }
                                });
    }

//    public void requestPermissions(int requestCode) {
//        try {
//            if (Build.VERSION.SDK_INT >= 23) {
//                ArrayList requestPerssionArr = new ArrayList<>();
//                int hasSdcardWrite = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                if (hasSdcardWrite != PackageManager.PERMISSION_GRANTED) {
//                    requestPerssionArr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                }
//                if (requestPerssionArr.size() >= 1) {
//                    String[] requestArray = new String[requestPerssionArr.size()];
//                    for (int i = 0; i < requestArray.length; i++) {
//                        requestArray[i] = String.valueOf(requestPerssionArr.get(i));
//                    }
//                    requestPermissions(requestArray, requestCode);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}