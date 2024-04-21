package com.example.sensorreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
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

    private Intent serviceIntent;
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
        IntentFilter filter = new IntentFilter("com.example.broadcast.THRESHOLD");
        registerReceiver(sensorUpdates, filter,RECEIVER_EXPORTED);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);     //Sensor Manager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);      //Sensor
        display_sensVal  = findViewById(R.id.textView_sensorVal);  //Display

        display_period  = findViewById(R.id.textView_period);

        SeekBar seekBarPeriod = findViewById(R.id.seekBar_period);                 //Period adjusted with seek bar
        seekBarPeriod.setMax(4);                                                   //*

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
                StartService(sensorPeriod, (int)threshold);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Add_button add clicklistener
        buttonnextActivity.setOnClickListener(v -> {
            if(serviceIntent!=null)                //When Called, stop current service and restart another service
                this.stopService(serviceIntent);
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
        prev_lux = event.values[0];
        display_sensVal.setText("Illuminance = " + event.values[0] + " lx");
    }
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
        serviceIntent.putExtra("type", 1); //0:Accelerometer, 1:Light
        this.startService(serviceIntent);
    }
    private Context contextBR = this;
    private BroadcastReceiver sensorUpdates = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.example.broadcast.THRESHOLD")){
                float data = intent.getFloatExtra("values",0);
                display_sensVal.setText("Value : "+data);
                Toast.makeText(contextBR, "Broadcast Received!", Toast.LENGTH_SHORT).show();
            }
        }
    };
}