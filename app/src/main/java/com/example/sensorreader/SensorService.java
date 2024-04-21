package com.example.sensorreader;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class SensorService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Intent broadcastIntent;
    private float sensorVal = 0;
    private Sensor accelerometer,lightsensor;
    private Timer periodicTimer;
    private int sensorPeriod = 5000;
    private TimerTask task_processor;
    private double threshold = 5.0; // Threshold
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightsensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        periodicTimer = new Timer();
        task_processor = periodicValCheck();
        broadcastIntent = new Intent();
        broadcastIntent.setAction("com.example.broadcast.THRESHOLD");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //startForeground(1, getNotification("Monitoring accelerometer..."));
        sensorPeriod = intent.getIntExtra("sensorPeriod",5000);
        threshold = intent.getIntExtra("threshold",5);
        if(intent.getIntExtra("type",0)==0)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        else
            sensorManager.registerListener(this, lightsensor, SensorManager.SENSOR_DELAY_NORMAL);
        notificationManager.createNotificationChannel(new NotificationChannel("channel_id","gyro_id",NotificationManager.IMPORTANCE_DEFAULT));
        periodicTimer.schedule(task_processor, 200, sensorPeriod); // delay is initial delay
        return START_STICKY;
    }

    private Notification getNotification(String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("Sensor Reader")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder.build();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                sensorVal = event.values[2];
            //Log.d("changingService", " z: "+z);
            else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                sensorVal = event.values[0];
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        if (task_processor != null) {
            task_processor.cancel();                                          //timer task is cancelled to avoid leaks
        }
        periodicTimer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){}

    private TimerTask periodicValCheck() {
        return new TimerTask() {
            @Override
            public void run() {  //periodically check if the threshold is exceeded
                if (Math.abs(sensorVal) < threshold) {
                    Log.d("tests", "run: "+sensorVal);
                    notificationManager.notify(2, getNotification("Threshold exceeded! "+sensorVal));
                    broadcastIntent.putExtra("values",sensorVal);
                    sendBroadcast(broadcastIntent);
                }
            }
        };
    }
}
