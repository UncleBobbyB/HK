package com.example.grad;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;


public class PoseParcel implements Parcelable {
    private Point[] points;

    protected PoseParcel(Parcel in) {
    }

    public static final Creator<PoseParcel> CREATOR = new Creator<PoseParcel>() {
        @Override
        public PoseParcel createFromParcel(Parcel in) {
            return null;
        }

        @Override
        public PoseParcel[] newArray(int size) {
            return new PoseParcel[size];
        }
    };

    public PoseParcel() {
    }

    public PoseParcel(Pose pose) {
        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks.isEmpty()) {
            points = null;
        }
        assert (landmarks.size() == 33);
        points = new Point[33];
        int cur = 0;
        for (int i = 0; i < landmarks.size(); i++) {
            points[i] = new Point(landmarks.get(i).getPosition().x,
                    landmarks.get(i).getPosition().y);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeArray(points);
    }
}
