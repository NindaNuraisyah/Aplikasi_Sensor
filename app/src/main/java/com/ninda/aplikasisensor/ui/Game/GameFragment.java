package com.ninda.aplikasisensor.ui.Game;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ninda.aplikasisensor.R;

public class GameFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private View carView;
    private TextView debugTextView;
    private float[] accelerometerValues = new float[3];
    private float[] gyroscopeValues = new float[3];

    private float carX, carY;
    private static final float CAR_SPEED = 0.1f;
    private static final float GYROSCOPE_SCALE = 1.0f;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_game, container, false);

        carView = root.findViewById(R.id.carView);
        debugTextView = root.findViewById(R.id.debugTextView);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (accelerometer != null) {
            sensorManager.unregisterListener(this, accelerometer);
        }
        if (gyroscope != null) {
            sensorManager.unregisterListener(this, gyroscope);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroscopeValues = event.values.clone();
        }
        updateCarPosition();
    }

    private void updateCarPosition() {
        carY -= accelerometerValues[1] * CAR_SPEED;
        carX += gyroscopeValues[1] * GYROSCOPE_SCALE;

        if (carX < 0) carX = 0;
        if (carY < 0) carY = 0;
        if (carX > ((ViewGroup) carView.getParent()).getWidth() - carView.getWidth())
            carX = ((ViewGroup) carView.getParent()).getWidth() - carView.getWidth();
        if (carY > ((ViewGroup) carView.getParent()).getHeight() - carView.getHeight())
            carY = ((ViewGroup) carView.getParent()).getHeight() - carView.getHeight();

        carView.setX(carX);
        carView.setY(carY);

        debugTextView.setText(String.format("X: %.2f, Y: %.2f, Gyro Y: %.2f", carX, carY, gyroscopeValues[1]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
