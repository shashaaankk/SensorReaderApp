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

import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class SensorService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private float gyro_z = 0;
    private Sensor accelerometer;
    private Timer periodicTimer;
    private int sensorPeriod = 5000;
    private TimerTask task_processor;
    private final double threshold = 5.0; // Threshold
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        periodicTimer = new Timer();
        task_processor = periodicValCheck();
        periodicTimer.schedule(task_processor, 200, sensorPeriod); // delay is initial delay
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //startForeground(1, getNotification("Monitoring accelerometer..."));
        notificationManager.createNotificationChannel(new NotificationChannel("channel_id","gyro_id",NotificationManager.IMPORTANCE_DEFAULT));
        getNotification("Monitoring accelerometer...");
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
            gyro_z = event.values[2];
            //Log.d("changingService", " z: "+z);
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
                if (Math.abs(gyro_z) < threshold) {
                    Log.d("tests", "run: "+gyro_z);
                    notificationManager.notify(2, getNotification("Threshold exceeded! "+gyro_z));
                }
            }
        };
    }
}
