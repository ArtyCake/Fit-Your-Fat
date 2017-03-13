package com.artycake.fityourfat.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.artycake.fityourfat.activities.MainActivity;
import com.artycake.fityourfat.R;
import com.artycake.fityourfat.models.Exercise;
import com.artycake.fityourfat.models.Workout;
import com.artycake.fityourfat.receivers.CallReceiver;
import com.artycake.fityourfat.receivers.TimerSoundReceiver;
import com.artycake.fityourfat.utils.RealmController;
import com.artycake.fityourfat.utils.TextHelper;
import com.artycake.fityourfat.utils.UserPrefs;

import io.realm.Realm;
import io.realm.RealmChangeListener;

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
    public static final String BROADCAST_UI_FILTER = "com.artycake.fityourfat.broadcast.ui";
    public static final String BROADCAST_SOUND_FILTER = "com.artycake.fityourfat.broadcast.sound";

    public static final String ACTION_TYPE = "action_type";
    public static final String TIMER_STARTED = "timer_started";
    public static final String TIMER_STOPPED = "timer_stopped";
    public static final String TIMER_PAUSED = "timer_paused";
    public static final String TIMER_RESUMED = "timer_resumed";
    public static final String SERVICE_STOPPED = "service_stopped";
    public static final String WORKOUT_CHANGED = "workout_changed";
    public static final String UPDATE_EXERCISE = "update_exercise";
    public static final String UE_NAME = "ue_name";
    public static final String UE_TIME = "ue_time";
    public static final String UE_DESC = "ue_desc";
    public static final String UE_PERCENT = "ue_percent";
    public static final String UE_LAPS = "ue_laps";
    public static final String UE_CURRENT_LAP = "ue_current_lap";
    public static final String WORKOUT_NAME = "workout_name";
    public static final String SOUND_WORKOUT_FINISHED = "sound_workout_finished";
    public static final String SOUND_EXERCISE_FINISHED = "sound_exercise_finished";
    public static final String SOUND_COUNTDOWN = "sound_countdown";
    public static final String SOUND_HALF = "sound_half";
    public static final String COUNTDOWN_SECONDS = "countdown_seconds";
    public static final String EXERCISE_NAME = "exercise_name";

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
    private RealmChangeListener<Realm> realmChangeListener;

    private TimerSoundReceiver timerSoundReceiver;
    private CallReceiver callReceiver;

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

    /* ----------- Lifecycle callbacks ---------- */

    @Override
    public void onCreate() {
        super.onCreate();
        timerBinder = new TimerBinder();
        final UserPrefs prefs = UserPrefs.getInstance(this);
        final RealmController realmController = RealmController.getInstance(this);
        currentWorkout = realmController.getWorkout(prefs.getIntPref(UserPrefs.CURRENT_WORKOUT, 0));
        currentExercise = currentWorkout.getExercises().first();
        // Define and register change listeners
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(UserPrefs.CURRENT_WORKOUT)) {
                    currentWorkout = realmController.getWorkout(prefs.getIntPref(UserPrefs.CURRENT_WORKOUT, 0));
                    stopTimer(true);
                    broadcastWorkoutChanged();
                }
            }
        };
        prefs.addOnPreferenceChange(preferenceChangeListener);
        realmChangeListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                Workout workout = realmController.getWorkout(currentWorkout.getId());
                if (workout == null) {
                    currentWorkout = realmController.getFirstWorkout();
                } else {
                    currentWorkout = workout;
                }
                stopTimer(true);
                broadcastWorkoutChanged();
            }
        };
        realmController.getRealm().addChangeListener(realmChangeListener);

        // Enable broadcast receivers
        timerSoundReceiver = new TimerSoundReceiver(this);
        registerReceiver(timerSoundReceiver, new IntentFilter(BROADCAST_SOUND_FILTER));
        callReceiver = new CallReceiver();
        registerReceiver(callReceiver, new IntentFilter("android.intent.action.PHONE_STATE"));
        registerReceiver(callReceiver, new IntentFilter("android.intent.action.NEW_OUTGOING_CALL"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove all change listeners before service will be destroyed
        UserPrefs.getInstance(this).removeOnPreferenceChange(preferenceChangeListener);
        RealmController.getInstance(this).getRealm().removeChangeListener(realmChangeListener);
        // Disable broadcast receivers
        unregisterReceiver(timerSoundReceiver);
        unregisterReceiver(callReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TAG serv", String.valueOf(intent));
        Log.d("TAG serv", String.valueOf(intent.getAction()));
        switch (intent.getAction()) {
            case ACTION_START_SERVICE: {
                notifyInit();
                break;
            }
            case ACTION_STOP_SERVICE: {
                stopForeground(true);
                stopSelf();
                broadcastSimple(SERVICE_STOPPED);
                break;
            }
            case ACTION_START_TIMER: {
                startTimer();
                break;
            }
            case ACTION_STOP_TIMER: {
                stopTimer(true);
                break;
            }
            case ACTION_PAUSE_TIMER: {
                pauseTimer();
                break;
            }
            case ACTION_RESUME_TIMER: {
                resumeTimer();
                break;
            }
        }

        return START_STICKY;
    }

    /* ----------- /Lifecycle callbacks ---------- */

    /* ----------- Main timer logic ---------- */

    private void startTimer() {
        broadcastSimple(TIMER_STARTED);
        timePassed = -1;
        timerHandler.postDelayed(timerRunnable, 0);
        started = true;
    }

    private void stopTimer(boolean reset) {
        broadcastSimple(TIMER_STOPPED);
        broadcastExercise();
        notifyTick();
        started = false;
        paused = false;
        currentExercise = currentWorkout.getExercises().first();
        currentLap = 1;
        timePassed = 0;
        if (reset) {
            broadcastExercise();
            notifyInit();
        }
    }

    private void pauseTimer() {
        broadcastSimple(TIMER_PAUSED);
        notifyPause();
        paused = true;
        pauseDiff = (int) (System.currentTimeMillis() - lastTimerTime);
    }

    private void resumeTimer() {
        broadcastSimple(TIMER_RESUMED);
        paused = false;
        timerHandler.postDelayed(timerRunnable, 1000 - pauseDiff);
    }

    private void tick() {
        if (!isStarted() || isPaused()) {
            return;
        }
        timePassed++;
        broadcastExercise();
        notifyTick();
        if (timePassed > 0 && timePassed == currentExercise.getDuration() / 2) {
            broadcastSoundSimple(SOUND_HALF);
        }
        int secondsLeft = currentExercise.getDuration() - timePassed;
        if (secondsLeft < 4 && secondsLeft > 0) {
            broadcastSoundCountdown(secondsLeft);
        }
        if (timePassed >= currentExercise.getDuration()) {
            Exercise next = currentWorkout.nextExercise();
            if (next == null) {
                if (currentLap < currentWorkout.getLaps()) {
                    currentLap++;
                    currentExercise = currentWorkout.getExercises().first();
                    timePassed = 0;
                    broadcastExercise();
                    notifyTick();
                    broadcastSoundExerciseFinished();
                } else {
                    stopTimer(false);
                    broadcastSoundSimple(SOUND_WORKOUT_FINISHED);
                }
            } else {
                currentExercise = next;
                timePassed = 0;
                broadcastExercise();
                notifyTick();
                broadcastSoundExerciseFinished();
            }
        }
    }

    /* ----------- /Main timer logic ---------- */

    /* ---------- Notifications ---------- */

    private void notifyInit() {
        Intent startIntent = new Intent(this, TimerService.class);
        startIntent.setAction(ACTION_START_TIMER);
        PendingIntent startPendingIntent = PendingIntent.getService(this, 0, startIntent, 0);
        Intent exitIntent = new Intent(this, TimerService.class);
        exitIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent exitPendingIntent = PendingIntent.getService(this, 0, exitIntent, 0);

        Notification notification = getBaseNotificationBuilder()
                .addAction(R.drawable.ic_play, getResources().getString(R.string.main_start_btn), startPendingIntent)
                .addAction(R.drawable.ic_exit, getResources().getString(R.string.main_exit_btn), exitPendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void notifyPause() {
        Intent resumeIntent = new Intent(this, TimerService.class);
        resumeIntent.setAction(ACTION_RESUME_TIMER);
        PendingIntent resumePendingIntent = PendingIntent.getService(this, 0, resumeIntent, 0);

        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        Notification notification = getBaseNotificationBuilder()
                .addAction(R.drawable.ic_stop, getResources().getString(R.string.main_stop_btn), stopPendingIntent)
                .addAction(R.drawable.ic_play, getResources().getString(R.string.main_resume_btn), resumePendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void notifyTick() {
        Intent pauseIntent = new Intent(this, TimerService.class);
        pauseIntent.setAction(ACTION_PAUSE_TIMER);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction(ACTION_STOP_TIMER);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        Notification notification = getBaseNotificationBuilder()
                .addAction(R.drawable.ic_stop, getResources().getString(R.string.main_stop_btn), stopPendingIntent)
                .addAction(R.drawable.ic_pause, getResources().getString(R.string.main_pause_btn), pausePendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private NotificationCompat.Builder getBaseNotificationBuilder() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Bitmap largeIcon;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            largeIcon = ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher, getTheme())).getBitmap();
        } else {
            largeIcon = ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap();
        }
        return new NotificationCompat.Builder(this)
                .setContentTitle(currentExercise.getName())
                .setContentText(TextHelper.formatTime(currentExercise.getDuration() - timePassed))
                .setTicker(getResources().getString(R.string.app_name))
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
    }

    /* ---------- /Notifications ---------- */

    /* ---------- Broadcasts ---------- */
    private void broadcastExercise() {
        String time = TextHelper.formatTime(currentExercise.getDuration() - timePassed);
        Intent intent = new Intent(BROADCAST_UI_FILTER);
        intent.putExtra(ACTION_TYPE, UPDATE_EXERCISE);
        intent.putExtra(UE_TIME, time);
        intent.putExtra(UE_NAME, currentExercise.getName());
        intent.putExtra(UE_DESC, currentExercise.getDescription());
        intent.putExtra(UE_PERCENT, 100 * timePassed / currentExercise.getDuration());
        intent.putExtra(UE_LAPS, currentWorkout.getLaps());
        intent.putExtra(UE_CURRENT_LAP, currentLap);
        sendBroadcast(intent);
        Log.d("TAG", "broadcast exercise");
    }

    private void broadcastWorkoutChanged() {
        Intent intent = new Intent(BROADCAST_UI_FILTER);
        intent.putExtra(ACTION_TYPE, WORKOUT_CHANGED);
        intent.putExtra(WORKOUT_NAME, currentWorkout.getName());
        sendBroadcast(intent);
        Log.d("TAG", "broadcast workout");
    }

    private void broadcastSimple(String action) {
        Intent intent = new Intent(BROADCAST_UI_FILTER);
        intent.putExtra(ACTION_TYPE, action);
        sendBroadcast(intent);
    }

    private void broadcastSoundCountdown(int secondsLeft) {
        Intent intent = new Intent(BROADCAST_SOUND_FILTER);
        intent.putExtra(ACTION_TYPE, SOUND_COUNTDOWN);
        intent.putExtra(COUNTDOWN_SECONDS, secondsLeft);
        sendBroadcast(intent);
    }

    private void broadcastSoundExerciseFinished() {
        Intent intent = new Intent(BROADCAST_SOUND_FILTER);
        intent.putExtra(ACTION_TYPE, SOUND_EXERCISE_FINISHED);
        intent.putExtra(EXERCISE_NAME, currentExercise.getName());
        sendBroadcast(intent);
    }

    private void broadcastSoundSimple(String action) {
        Intent intent = new Intent(BROADCAST_SOUND_FILTER);
        intent.putExtra(ACTION_TYPE, action);
        sendBroadcast(intent);
    }

    /* ---------- /Broadcasts ---------- */

    public boolean isStarted() {
        return started;
    }

    public boolean isPaused() {
        return paused;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        broadcastWorkoutChanged();
        broadcastExercise();
        return timerBinder;
    }

    public class TimerBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("TAG", "rebound");
        super.onRebind(intent);
        broadcastWorkoutChanged();
        broadcastExercise();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }
}
