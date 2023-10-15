package com.example.semana8;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private ImageView mImageView;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private SensorEventListener sensorEventListener;

    private float[] rotationMatrix = new float[9];
    private float[] orientationValues = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.mImageView);
        Button downloadButton = findViewById(R.id.downloadButton);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        final Bitmap bitmap = loadImageFromNetwork("https://smartcdn.gprod.postmedia.digital/theprovince/wp-content/uploads/2015/11/astley.jpg?quality=90&strip=all&w=564&h=423&type=webp&sig=vyujr4fBxhChZwjntC__7w");
                        mImageView.post(new Runnable() {
                            public void run() {
                                mImageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }).start();
            }
        });

        // Configuración del sensor de rotación
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Crear un SensorEventListener para la detección de cambios en la orientación
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                    getRotationMatrixFromRotationVector(rotationMatrix, event.values);
                    SensorManager.getOrientation(rotationMatrix, orientationValues);

                    // Obtener el ángulo de rotación
                    float azimuth = (float) Math.toDegrees(orientationValues[0]);
                    float pitch = (float) Math.toDegrees(orientationValues[1]);
                    float roll = (float) Math.toDegrees(orientationValues[2]);

                    // Rotar la imagen según el ángulo de roll
                    mImageView.setRotation(-roll);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Método no utilizado
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registra el SensorEventListener para el sensor de rotación
        sensorManager.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detiene la escucha del sensor al salir de la actividad
        sensorManager.unregisterListener(sensorEventListener);
    }

    // Método para descargar una imagen desde una URL
    private Bitmap loadImageFromNetwork(String url) {
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método personalizado para obtener la matriz de rotación a partir del vector de rotación
    private void getRotationMatrixFromRotationVector(float[] rotationMatrix, float[] rotationVector) {
        float q0 = rotationVector[0];
        float q1 = rotationVector[1];
        float q2 = rotationVector[2];
        float q3 = -rotationVector[3]; // Cambiar el signo del componente w

        rotationMatrix[0] = 1 - 2 * q2 * q2 - 2 * q3 * q3;
        rotationMatrix[1] = 2 * q1 * q2 - 2 * q0 * q3;
        rotationMatrix[2] = 2 * q1 * q3 + 2 * q0 * q2;
        rotationMatrix[3] = 2 * q1 * q2 + 2 * q0 * q3;
        rotationMatrix[4] = 1 - 2 * q1 * q1 - 2 * q3 * q3;
        rotationMatrix[5] = 2 * q2 * q3 - 2 * q0 * q1;
        rotationMatrix[6] = 2 * q1 * q3 - 2 * q0 * q2;
        rotationMatrix[7] = 2 * q2 * q3 + 2 * q0 * q1;
        rotationMatrix[8] = 1 - 2 * q1 * q1 - 2 * q2 * q2;
    }
}