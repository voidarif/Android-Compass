package com.nexthopetech.compass;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    ImageView compass_img;
    TextView azimuth_txt;
    int azimuth;

    private SensorManager sensorManager;
    private Sensor rotationVector, accelerometer, magnetometer;
    float[] mat = new float[9];
    float[] orientation = new float[9];

    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];

    private boolean haveSensor = false, haveSensor2 = false;
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compass_img = findViewById(R.id.compass_img);
        azimuth_txt = findViewById(R.id.azimuth_txt);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(mat, event.values);
            azimuth = (int) (Math.toDegrees(SensorManager.getOrientation(mat, orientation)[0]) + 360 % 360);
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
                lastMagnetometerSet = true;
            }
            if (lastMagnetometerSet && lastAccelerometerSet) {
                SensorManager.getRotationMatrix(mat, null, lastAccelerometer, lastMagnetometer);
                SensorManager.getOrientation(mat, orientation);
                azimuth = (int) (Math.toDegrees(SensorManager.getOrientation(mat, orientation)[0]) + 360 % 360);
            }
            azimuth = Math.round(azimuth);
            compass_img.setRotation(-azimuth);

            String where = "NO";

            if (azimuth >= 350 || azimuth <= 10)
                where = "N";
            if (azimuth < 350 && azimuth > 280)
                where = "NW";
            if (azimuth <= 280 && azimuth > 260)
                where = "W";
            if (azimuth <= 260 && azimuth > 190)
                where = "SW";
            if (azimuth <= 190 && azimuth > 170)
                where = "S";
            if (azimuth <= 170 && azimuth > 100)
                where = "SE";
            if (azimuth <= 100 && azimuth > 80)
                where = "E";
            if (azimuth <= 80 && azimuth > 10)
                where = "NE";

            azimuth_txt.setText(Math.abs(azimuth) +"° "+where);

    }

        @Override
        public void onAccuracyChanged (Sensor sensor,int i){

        }

        public void start () {
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
                if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null || sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
                    noSensorAlert();
                } else {
                    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    magnetometer = sensorManager.getDefaultSensor((Sensor.TYPE_MAGNETIC_FIELD));

                    haveSensor = sensorManager.registerListener(this, accelerometer, sensorManager.SENSOR_DELAY_UI);
                    haveSensor2 = sensorManager.registerListener(this, magnetometer, sensorManager.SENSOR_DELAY_UI);


                }
            } else {
                rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                haveSensor = sensorManager.registerListener(this, rotationVector, sensorManager.SENSOR_DELAY_UI);
            }
        }

        public void noSensorAlert () {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Your Device Does not support the compass")
                    .setCancelable(false)
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
        }

        public void stop () {
            if (haveSensor && haveSensor2) {
                sensorManager.unregisterListener(this, accelerometer);
                sensorManager.unregisterListener(this, magnetometer);
            } else {
                if (haveSensor) {
                    sensorManager.unregisterListener(this, rotationVector);
                }
            }
        }

        @Override
        protected void onPause () {
            super.onPause();
            stop();
        }
        @Override
        protected void onResume () {
            super.onResume();
            start();
        }
    }
