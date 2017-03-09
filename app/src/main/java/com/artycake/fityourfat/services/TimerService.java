package com.artycake.fityourfat.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.artycake.fityourfat.activities.MainActivity;
import com.artycake.fityourfat.R;
import com.artycake.fityourfat.models.Exercise;
import com.artycake.fityourfat.models.Workout;
import com.artycake.fityourfat.utils.RealmController;
import com.artycake.fityourfat.utils.TextHelper;
import com.artycake.fityourfat.utils.UserPrefs;

import io.realm.RealmList;

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
    public static final String BROADCAST_FILTER = "com.artycake.fityourfat.broadcast.tick";

    public static final String ACTION_TYPE = "action_type";
    public static final String TIMER_STARTED = "timer_started";
    public static final String TIMER_STOPPED = "timer_stopped";
    public static final String TIMER_PAUSED = "timer_paused";
    public static final String TIMER_RESUMED = "timer_resumed";
    public static final String UPDATE_EXERCISE = "update_exercise";
    public static final String UE_NAME = "ue_name";
    public static final String UE_TIME = "ue_time";
    public static final String UE_DESC = "ue_desc";
    public static final String UE_PERCENT = "ue_percent";
    public static final String UE_LAPS = "ue_laps";
    public static final String UE_CURRENT_LAP = "ue_current_lap";

    private TimerBinder timerBinder;
    private Handler timerHandler = new Handler();
    private boolean started = false;
    private boolean paused = false;
    private Workout currentWorkout;
    private Exercise currentExercise;
    private int currentLap = 1;
    private int timePassed = 0;
    private int pauseDiff;
    private long lastTimerTime;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    private Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            lastTimerTime = System.currentTimeMillis();
            tick();
            if (isStarted() && !isPaused()) {
                timerHandler.postDelayed(timerRunnable, 1000);
            }
        }
    };

    private void tick() {
        if (!isStarted() || isPaused()) {
            return;
        }
        timePassed++;
        sendExerciseBroadcast();
        showTickNotification();
        if (timePassed >= currentExercise.getDuration()) {
            Exercise next = currentWorkout.nextExercise();
            if (next == null) {
                if (currentLap < currentWorkout.getLaps()) {
                    currentLap++;
                    currentExercise = currentWorkout.getExercises().first();
                    timePassed = 0;
                    sendExerciseBroadcast();
                } else {
                    stopTimer(false);
                }
            } else {
                currentExercise = next;
                timePassed = 0;
                sendExerciseBroadcast();
            }
        }
    }

    private void sendExerciseBroadcast() {
        String time = TextHelper.formatTime(currentExercise.getDuration() - timePassed);
        Intent intent = new Intent(BROADCAST_FILTER);
        intent.putExtra(ACTION_TYPE, UPDATE_EXERCISE);
        intent.putExtra(UE_TIME, time);
        intent.putExtra(UE_NAME, currentExercise.getName());
        intent.putExtra(UE_DESC, currentExercise.getDescription());
        intent.putExtra(UE_PERCENT, 100 * timePassed / currentExercise.getDuration());
        intent.putExtra(UE_LAPS, currentWorkout.getLaps());
        intent.putExtra(UE_CURRENT_LAP, currentLap);
        sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timerBinder = new TimerBinder();
        final UserPrefs prefs = UserPrefs.getInstance(this);
        final RealmController realmController = RealmController.getInstance(this);
        currentWorkout = realmController.getWorkout(prefs.getIntPref(UserPrefs.CURRENT_WORKOUT, 0));
        currentExercise = currentWorkout.getExercises().first();
        sendExerciseBroadcast();
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(UserPrefs.CURRENT_WORKOUT)) {
                    currentWorkout = realmController.getWorkout(prefs.getIntPref(UserPrefs.CURRENT_WORKOUT, 0));
                    fullReset();
                }
            }
        };
        prefs.addOnPreferenceChange(preferenceChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UserPrefs.getInstance(this).removeOnPreferenceChange(preferenceChangeListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
                sendSimpleBroadcast(TIMER_STARTED);
                resetWorkout();
                timePassed = -1;
                started = true;
                timerHandler.postDelayed(timerRunnable, 0);
                break;
            }
            case ACTION_STOP_TIMER: {
                Log.d("FORSERVICE", "ACTION_STOP_TIMER");
                stopTimer(true);
                break;
            }
            case ACTION_PAUSE_TIMER: {
                Log.d("FORSERVICE", "ACTION_PAUSE_TIMER");
                sendSimpleBroadcast(TIMER_PAUSED);
                paused = true;
                pauseDiff = (int) (System.currentTimeMillis() - lastTimerTime);
                showPauseNotification();
                break;
            }
            case ACTION_RESUME_TIMER: {
                Log.d("FORSERVICE", "ACTION_RESUME_TIMER");
                sendSimpleBroadcast(TIMER_RESUMED);
                paused = false;
                timerHandler.postDelayed(timerRunnable, 1000 - pauseDiff);
                break;
            }
        }

        return START_STICKY;
    }

    private void resetWorkout() {
        currentExercise = currentWorkout.getExercises().first();
        started = false;
        paused = false;
    }

    private void fullReset() {
        resetWorkout();
        sendExerciseBroadcast();
        showStartNotification();
        sendSimpleBroadcast(TIMER_STOPPED);
    }

    private void stopTimer(boolean reset) {
        sendSimpleBroadcast(TIMER_STOPPED);
        started = false;
        paused = false;
        timePassed = 0;
        if (reset) {
            fullReset();
        }
    }

    private void startTimerService() {
        showStartNotification();
    }

    private void showStartNotification() {
        Intent startIntent = new Intent(this, TimerService.class);
        startIntent.setAction(ACTION_START_TIMER);
        PendingIntent startPendingIntent = PendingIntent.getService(this, 0, startIntent, 0);

        Notification notification = getBaseNotificationBuilder()
                .addAction(android.R.drawable.ic_media_play, getResources().getString(R.string.main_start_btn), startPendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void showPauseNotification() {
        Intent resumeIntent = new Intent(this, TimerService.class);
        resumeIntent.setAction(ACTION_RESUME_TIMER);
        PendingIntent resumePendingIntent = PendingIntent.getService(this, 0, resumeIntent, 0);

        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        Notification notification = getBaseNotificationBuilder()
                .addAction(android.R.drawable.ic_media_pause, getResources().getString(R.string.main_stop_btn), stopPendingIntent)
                .addAction(android.R.drawable.ic_media_play, getResources().getString(R.string.main_resume_btn), resumePendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void showTickNotification() {
        Intent pauseIntent = new Intent(this, TimerService.class);
        pauseIntent.setAction(ACTION_PAUSE_TIMER);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        Notification notification = getBaseNotificationBuilder()
                .addAction(android.R.drawable.ic_media_pause, getResources().getString(R.string.main_stop_btn), stopPendingIntent)
                .addAction(android.R.drawable.ic_media_pause, getResources().getString(R.string.main_pause_btn), pausePendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private NotificationCompat.Builder getBaseNotificationBuilder() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        return new NotificationCompat.Builder(this)
                .setContentTitle(currentExercise.getName())
                .setContentText(TextHelper.formatTime(currentExercise.getDuration() - timePassed))
                .setTicker(getResources().getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
    }

    private void sendSimpleBroadcast(String action) {
        Intent intent = new Intent(BROADCAST_FILTER);
        intent.putExtra(ACTION_TYPE, action);
        sendBroadcast(intent);
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isPaused() {
        return paused;
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
