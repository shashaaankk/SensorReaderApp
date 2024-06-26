package com.example.sensorreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private TextView display_sensVal;
    private TextView display_period;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    final private double threshold = 5.0; // Default threshold
    private int sensorPeriod = 200;       // Default 1ms*
    private boolean sensorListnerRegistered;
    private double prev_roundedz;
    private Button second_activity_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    //Initializing Broadcasting
        IntentFilter filter = new IntentFilter("com.example.broadcast.THRESHOLD");
        registerReceiver(sensorUpdates, filter,RECEIVER_EXPORTED);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);          //Sensor Manager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //Sensor
        display_sensVal  = findViewById(R.id.textView_sensorVal);                  //Display

        second_activity_button = findViewById(R.id.second_activity_button);

        display_period  = findViewById(R.id.textView_period);                      //Display
        SeekBar seekBarPeriod = findViewById(R.id.seekBar_period);                 //Period adjusted with seek bar
        seekBarPeriod.setMax(4);                                                   //*
        //periodicTimer = new Timer();                                               //Initialization of Timer
        //task_processor = periodicValCheck();                                       //Initialization of Timer Task

        TextView display_threshold = findViewById(R.id.textView_threshold);        //Display Pre-Set Threshold
        display_threshold.setText("Threshold: " + threshold + " m/s^2");

        // for setting period
        seekBarPeriod.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                sensorPeriod = (progress+1)*1000;                                 //ms; +1 added to prevent 0 period
                display_period.setText("Period: " + sensorPeriod + "ms");         //Display
                StartService(sensorPeriod, (int)threshold);
                // Reschedule the timer task
                //task_processor.cancel();                                        // Cancel the current task
                //task_processor = periodicValCheck();                        // Create a new task
                //periodicTimer.schedule(task_processor, 200, sensorPeriod);  // Schedule with new period
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        //Second page
        second_activity_button.setOnClickListener(v -> {
            if(serviceIntent!=null)  //When Called, stop current service and restart another service
            {
                this.stopService(serviceIntent);
            }
            Intent intent = new Intent(MainActivity.this, LightSensActivity.class);
            startActivity(intent);

        });

    }
    @Override
    protected void onStart(){
        super.onStart();
        prev_roundedz = 0.0;
        if (sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)){
            sensorListnerRegistered = true;
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
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float roundedx = Math.round(event.values[0] * 100.0f) / 100.0f;
        float roundedy = Math.round(event.values[1] * 100.0f) / 100.0f;
        float roundedz = Math.round(event.values[2] * 100.0f) / 100.0f;
        prev_roundedz = roundedz;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            display_sensVal.setText("X:  " + roundedx + "| Y:  " + roundedy + "| Z:  " + roundedz + " m/s^2");
        }
    }
    private Intent serviceIntent;
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void StartService(int period,int threshold){
        if(serviceIntent!=null)                //When Called, stop current service and restart another service
        {
            this.stopService(serviceIntent);
        }
        serviceIntent = new Intent(this,SensorService.class);
        serviceIntent.putExtra("sensorPeriod", period);
        serviceIntent.putExtra("threshold", threshold);
        serviceIntent.putExtra("type", 0); //0:Accelerometer, 1:Light
        this.startService(serviceIntent);
    }
    private Context contextBR = this;
    private BroadcastReceiver sensorUpdates = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.example.broadcast.THRESHOLD")){
                float data = intent.getFloatExtra("values",0);
                Log.d("cbjhskdkjf", "onReceive: "+data);
                display_sensVal.setText("Broadcast Received: "+data);
                Toast.makeText(contextBR, "Broadcast Received!", Toast.LENGTH_SHORT).show();
            }
        }
    };
}