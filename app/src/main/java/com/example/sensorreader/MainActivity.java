package com.example.sensorreader;

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

public class MainActivity extends AppCompatActivity {

    private TextView display_sensVal, display_period, display_threshold;   // TextView to display the sensor Values
    private SeekBar seekBarThreshold;   // SeekBar for adjusting the threshold
    private SeekBar seekBarPeriod;
    private Button buttonnextActivity;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor magnetometer;
    private int threshold = 1; // Default threshold

    public static final int TYPE_LIGHT =0;
    private SensorEventListener sensorEventListenerLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            setupSensors();
            return insets;
        });

        display_period  = (TextView) findViewById(R.id.textView_period);
        display_threshold  = (TextView) findViewById(R.id.textView_threshold);
        display_sensVal  = (TextView) findViewById(R.id.textView_sensorVal);

        seekBarThreshold = (SeekBar) findViewById(R.id.seekBar_threshold);
        seekBarPeriod = (SeekBar) findViewById(R.id.seekBar_period);
        buttonnextActivity = (Button) findViewById(R.id.first_activity_button);

        // for the threshold
        seekBarThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                display_threshold.setText("Threshold: " + progress + " lx");
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
        // for period
        seekBarPeriod.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int sensorPeriod = progress + 200;
                display_period.setText("Period: " + sensorPeriod + "ms");
                //Period goes from 200ms to 1000ms
                if (fromUser)
                {
                  updateSensorPeriod(sensorPeriod);
                }
            }

            private void updateSensorPeriod(int period) {
                // Unregister the existing sensor listener
                if (lightSensor != null) {
                    sensorManager.unregisterListener(sensorEventListenerLight, lightSensor);
                }
                // Re-register the listener with the new period
                int delay = period * 1000;  // Convert ms to microseconds as needed by Android sensors
                if (lightSensor != null) {
                    sensorManager.registerListener(sensorEventListenerLight, lightSensor, delay);
                }
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
        buttonnextActivity.setOnClickListener(v -> {
            // Intents are objects of the android.content.Intent type. Your code can send them to the Android system defining
            // the components you are targeting. Intent to start an activity called SecondActivity with the following code.
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            // start the activity connect to the specified class
            startActivity(intent);
        });
    }
    private void setupSensors() {
        //sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(lightSensor == null){
            Toast.makeText(this, "Device has no light sensor!", Toast.LENGTH_SHORT).show();
        }

        SensorEventListener sensorEventListenerLight = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                display_sensVal.setText("Illuminance = " + event.values[0] + " lx");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(sensorEventListenerLight, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener((SensorEventListener) this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
}