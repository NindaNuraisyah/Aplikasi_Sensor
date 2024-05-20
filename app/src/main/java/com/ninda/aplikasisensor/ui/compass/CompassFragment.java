package com.ninda.aplikasisensor.ui.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ninda.aplikasisensor.R;

public class CompassFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private TextView compassText;
    private ImageView compassImage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_compass, container, false);

        compassText = root.findViewById(R.id.compassText);
        compassImage = root.findViewById(R.id.compassImage);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (rotationVectorSensor != null) {
            sensorManager.unregisterListener(this, rotationVectorSensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            handleRotationVectorSensor(event);
        }
    }

    private void handleRotationVectorSensor(SensorEvent event) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

        float[] orientationAngles = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        float azimuth = (float) Math.toDegrees(orientationAngles[0]);
        if (azimuth < 0) {
            azimuth += 360;
        }

        compassText.setText(String.format("Azimuth: %.0f°", azimuth));
        compassImage.setRotation(-azimuth);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
