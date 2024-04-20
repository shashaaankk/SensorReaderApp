package com.example.sensorreader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.hardware.lights.Light;
import android.hardware.GeomagneticField.*;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class SecondActivity extends AppCompatActivity implements SensorEventListener{

    private TextView display_sensVal, display_period, display_threshold;   // TextView to display the sensor Values
    private SeekBar seekBarPeriod;
    private Button buttonpreviousActivity;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Timer periodicTimer;
    final private double threshold = 5.0; // Default threshold
    private SensorEventListener sensorEventListenerAccelerometer;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            Timer periodicTimer = new Timer();
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            setupSensors();
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            return insets;
        });

        display_period  = (TextView) findViewById(R.id.textView_period);
        seekBarPeriod = (SeekBar) findViewById(R.id.seekBar_period);

        display_threshold  = (TextView) findViewById(R.id.textView_threshold);
        display_threshold.setText("Threshold: " + threshold + " m/s^2");

        display_sensVal  = (TextView) findViewById(R.id.textView_sensorVal);

        buttonpreviousActivity = (Button) findViewById(R.id.second_activity_button);

        // for period
        seekBarPeriod.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int sensorPeriod = progress+200;
                display_period.setText("Period: " + sensorPeriod + "ms");
                periodicTimer.schedule(task_processor, 0, sensorPeriod);
            }
            @Override

            public void onStartTrackingTouch(SeekBar seekBar) {
                //functionality when the user starts touching the SeekBar
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //functionality when the user stops touching the SeekBar
            }
        });
        // Add_button add clicklistener
        buttonpreviousActivity.setOnClickListener(v -> {
            // Intents are objects of the android.content.Intent type. Your code can send them to the Android system defining
            // the components you are targeting. Intent to start an activity called SecondActivity with the following code.
            Intent intent = new Intent(SecondActivity.this, MainActivity.class);
            // start the activity connect to the specified class
            startActivity(intent);
        });
    }

    private void setupSensors() {
        //sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            Toast.makeText(this, "Device has no light sensor!", Toast.LENGTH_SHORT).show();

        }
    }
    TimerTask task_processor = new TimerTask() {
        public void run() {

        }
    };
    @Override
    public void onSensorChanged(SensorEvent event) {
        float roundedx = Math.round(event.values[0] * 100.0f) / 100.0f;
        float roundedy = Math.round(event.values[1] * 100.0f) / 100.0f;
        float roundedz = Math.round(event.values[2] * 100.0f) / 100.0f;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            display_sensVal.setText("X:  " + roundedx + "| Y:  " + roundedy + "| Z:  " + roundedz + " m/s^2");
            if (Math.abs(roundedz) > threshold) {
                //Toast
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Not Used
    }
}