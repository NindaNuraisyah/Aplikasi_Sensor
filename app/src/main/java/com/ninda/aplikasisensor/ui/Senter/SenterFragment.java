package com.ninda.aplikasisensor.ui.Senter;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ninda.aplikasisensor.R;

public class SenterFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private RelativeLayout layout;
    private ImageView imageViewFlash;
    private TextView warningTextView;
    private Vibrator vibrator;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean flashOn = false;
    private Handler handler = new Handler();
    private Runnable flashRunnable;
    private MediaPlayer mediaPlayer;
    private static final float THRESHOLD_VALUE = 10.0f; // Ambang cahaya yang digunakan untuk menentukan kondisi gelap

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_senter, container, false);

        layout = root.findViewById(R.id.layout);
        imageViewFlash = root.findViewById(R.id.imageViewFlash);
        warningTextView = root.findViewById(R.id.warningTextView);
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.gelap);

        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (Exception e) {
            e.printStackTrace();
        }

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            warningTextView.setText("Sensor cahaya tidak tersedia");
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (lightSensor != null) {
            sensorManager.unregisterListener(this);
        }
        stopFlash();
        pauseMediaPlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopFlash();
        pauseMediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopFlash();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private boolean wasDark = false; // Menyimpan status lingkungan sebelumnya

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightValue = event.values[0];
            if (lightValue < THRESHOLD_VALUE) { // Jika lingkungan gelap
                layout.setBackgroundColor(Color.BLACK);
                warningTextView.setText("Lingkungan Gelap\nSenter Menyala!");
                warningTextView.setTextColor(Color.WHITE);
                if (!flashOn) {
                    startFlash();
                }
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }
                imageViewFlash.setImageResource(R.drawable.flashon);
                wasDark = true; // Set status lingkungan menjadi gelap
            } else {
                layout.setBackgroundColor(Color.WHITE);
                warningTextView.setText("Lingkungan Terang\nSenter Mati!");
                warningTextView.setTextColor(Color.BLACK);
                stopFlash();
                pauseMediaPlayer(); // Jeda pemutaran media player saat lingkungan terang
                imageViewFlash.setImageResource(R.drawable.flashoff);
                wasDark = false; // Set status lingkungan menjadi terang
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void startFlash() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, true);
                flashOn = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopFlash() {
        try {
            handler.removeCallbacks(flashRunnable);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, false);
                flashOn = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pauseMediaPlayer() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0); // Kembali ke awal
        }
    }
}
