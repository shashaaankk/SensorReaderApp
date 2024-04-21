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
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class LightSensActivity extends AppCompatActivity implements SensorEventListener{

    private TextView display_sensVal, display_period;   // TextView to display the sensor Values

    private Timer periodicTimer;
    private TimerTask task_processor;
    private Button buttonnextActivity;
    private SensorManager sensorManager;
    private boolean sensorListnerRegistered;
    private Sensor lightSensor;
    private double prev_lux;
    final private double threshold = 100.0; // Default threshold (lux)
    private int sensorPeriod = 200;       // Default 1ms*

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.lightsensactivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);     //Sensor Manager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);      //Sensor
        display_sensVal  = findViewById(R.id.textView_sensorVal);  //Display

        display_period  = findViewById(R.id.textView_period);

        SeekBar seekBarPeriod = findViewById(R.id.seekBar_period);                 //Period adjusted with seek bar
        seekBarPeriod.setMax(4);                                                   //*
        periodicTimer = new Timer();                                               //Initialization of Timer
        task_processor = periodicValCheck();                                       //Initialization of Timer Task

        buttonnextActivity = findViewById(R.id.first_activity_button);

        TextView display_threshold = findViewById(R.id.textView_threshold);        //Display Pre-Set Threshold
        display_threshold.setText("Threshold: " + threshold + " lux");

        // for setting period
        seekBarPeriod.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                sensorPeriod = (progress+1)*1000;                                 //ms; +1 added to prevent 0 period
                display_period.setText("Period: " + sensorPeriod + "ms");         //Display
                // Reschedule the timer task
                task_processor.cancel();                                          // Cancel the current task
                task_processor = periodicValCheck();                              // Create a new task
                periodicTimer.schedule(task_processor, 200, sensorPeriod);  // Schedule with new period
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Add_button add clicklistener
        buttonnextActivity.setOnClickListener(v -> {
            Intent intent = new Intent(LightSensActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
    @Override
    protected void onStart(){
        super.onStart();
        prev_lux = 0.0;
        if (sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)){
            sensorListnerRegistered = true;
            periodicTimer = new Timer();                                      // Reinitialize the timer
            task_processor = periodicValCheck();                              // Reinitialize the task
            periodicTimer.schedule(task_processor, 200, sensorPeriod);  // Start the timer with period = sensorPeriod
        }
        else {
            Toast.makeText(this, "Sensor Listener could not be registered!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected  void onStop() {
        super.onStop();
        if(sensorListnerRegistered){
            sensorManager.unregisterListener(this);
        }
        if (task_processor != null) {
            task_processor.cancel();                                          //timer task is cancelled to avoid leaks
        }
        periodicTimer.cancel();                                               // Cancel the entire timer
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        prev_lux = event.values[0];
        display_sensVal.setText("Illuminance = " + event.values[0] + " lx");
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private TimerTask periodicValCheck() {
        return new TimerTask() {
            @Override
            public void run() {  //periodically check if the threshold is exceeded
                if (Math.abs(prev_lux) < threshold) {
                    for(int i=0; i<1000; i++) {
                        display_sensVal.setText("Threshold Exceeded!");
                    }
                }
            }
        };
    }
}