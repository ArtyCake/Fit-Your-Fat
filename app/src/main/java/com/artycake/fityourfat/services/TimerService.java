package com.artycake.fityourfat.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.artycake.fityourfat.MainActivity;
import com.artycake.fityourfat.R;

/**
 * Created by artycake on 3/7/17.
 */

public class TimerService extends Service {

    private static final int NOTIFICATION_ID = 1;
    public static final String ACTION_START_SERVICE = "com.artycake.fityourfat.action.startservice";
    public static final String ACTION_STOP_SERVICE = "com.artycake.fityourfat.action.stopservice";
    public static final String ACTION_START_TIMER = "com.artycake.fityourfat.action.starttimer";
    public static final String ACTION_STOP_TIMER = "com.artycake.fityourfat.action.stoptimer";
    public static final String ACTION_PAUSE_TIMER = "com.artycake.fityourfat.action.pausetimer";
    public static final String ACTION_RESUME_TIMER = "com.artycake.fityourfat.action.resumetimer";

    private TimerBinder timerBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        timerBinder = new TimerBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("FORSERVICE", "service started");
        Log.d("FORSERVICE", intent.getAction());
        switch (intent.getAction()) {
            case ACTION_START_SERVICE: {
                Log.d("FORSERVICE", "ACTION_START_SERVICE");
                startTimerService();
                break;
            }
            case ACTION_STOP_SERVICE: {
                Log.d("FORSERVICE", "ACTION_STOP_SERVICE");
                stopForeground(true);
                stopSelf();
                break;
            }
            case ACTION_START_TIMER: {
                Log.d("FORSERVICE", "ACTION_START_TIMER");
                break;
            }
            case ACTION_STOP_TIMER: {
                Log.d("FORSERVICE", "ACTION_STOP_TIMER");
                break;
            }
            case ACTION_PAUSE_TIMER: {
                Log.d("FORSERVICE", "ACTION_PAUSE_TIMER");
                break;
            }
            case ACTION_RESUME_TIMER: {
                Log.d("FORSERVICE", "ACTION_RESUME_TIMER");
                break;
            }
        }

        return START_STICKY;
    }

    private void startTimerService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent startIntent = new Intent(this, TimerService.class);
        startIntent.setAction(ACTION_START_TIMER);
        PendingIntent startPendingIntent = PendingIntent.getService(this, 0, startIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Workout")
                .setTicker(getResources().getString(R.string.app_name))
                .setContentText("Lift Up")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_play, "Start", startPendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return timerBinder;
    }

    public class TimerBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }
}
