package com.artycake.fityourfat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.services.TimerService;
import com.artycake.fityourfat.utils.TextHelper;
import com.artycake.fityourfat.utils.UserPrefs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class TimerSoundReceiver extends BroadcastReceiver {
    private TextToSpeech textToSpeech;
    private AudioManager audioManager;
    private UserPrefs userPrefs;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    private int defaultMusicVolume;
    private int defaultNotificationVolume;

    private boolean released = true;

    private static final String MESSAGE_ID = "timerService.messageId";

    public TimerSoundReceiver(final Context context) {
        userPrefs = UserPrefs.getInstance(context);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        Uri myUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.beep);
        try {
            mediaPlayer.setDataSource(context, myUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("MediaPlayer", "player prepared");
            }
        });
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.ERROR) {
                    return;
                }
                textToSpeech.setLanguage(TextHelper.getCurrentLocale());
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        prepareVolume();
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        releaseVolume();
                    }

                    @Override
                    public void onError(String utteranceId) {
                        releaseVolume();
                    }
                });
            }
        });
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        vibrate(intent);
        speak(intent);
        beep(intent);
    }

    private void beep(Intent intent) {
        if (!userPrefs.getBoolPref(UserPrefs.SOUNDS_ON, false)) {
            return;
        }
        switch (intent.getStringExtra(TimerService.ACTION_TYPE)) {
            case TimerService.SOUND_WORKOUT_FINISHED: {
                if (userPrefs.getBoolPref(UserPrefs.SOUNDS_WORKOUT_FINISH, false)) {
                    prepareVolume();
                    final int[] playedTimes = new int[]{0};
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            playedTimes[0]++;
                            if (playedTimes[0] == 3) {
                                releaseVolume();
                            } else {
                                mediaPlayer.start();
                            }
                        }
                    });
                    mediaPlayer.start();
                }
                break;
            }
            case TimerService.SOUND_EXERCISE_FINISHED: {
                if (userPrefs.getBoolPref(UserPrefs.SOUNDS_EXERCISE_FINISH, false)) {
                    prepareVolume();
                    Log.d("TAG SOUND", "playing beep 1 time");
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            releaseVolume();
                        }
                    });
                }
                break;
            }
        }
    }

    private void releaseVolume() {
        Log.d("TAG SOUND", "volume released");
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultMusicVolume, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, defaultNotificationVolume, 0);
        released = true;
    }

    private void prepareVolume() {
        if (!released) {
            return;
        }
        released = false;
        defaultMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        defaultNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 10, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) / 2, 0);
    }

    private void speak(final Intent intent) {
        if (!userPrefs.getBoolPref(UserPrefs.VOICE_ON, false)) {
            return;
        }
        if (textToSpeech == null) {
            return;
        }

        HashMap<String, String> paramsMap = new HashMap<>();
        Bundle paramsBundle = new Bundle();
        paramsMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
        paramsMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, MESSAGE_ID);
        paramsBundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_NOTIFICATION);
        switch (intent.getStringExtra(TimerService.ACTION_TYPE)) {
            case TimerService.SOUND_HALF: {
                if (userPrefs.getBoolPref(UserPrefs.VOICE_HALF, false)) {
                    prepareVolume();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak("Half time", TextToSpeech.QUEUE_FLUSH, paramsBundle, MESSAGE_ID);
                    } else {
                        textToSpeech.speak("Half time", TextToSpeech.QUEUE_FLUSH, paramsMap);
                    }
                }
                break;
            }
            case TimerService.SOUND_COUNTDOWN: {
                if (userPrefs.getBoolPref(UserPrefs.VOICE_COUNTDOWN, false)) {
                    int number = intent.getIntExtra(TimerService.COUNTDOWN_SECONDS, -1);
                    if (number == -1) {
                        return;
                    }
                    prepareVolume();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(String.valueOf(number), TextToSpeech.QUEUE_FLUSH, paramsBundle, null);
                    } else {
                        textToSpeech.speak(String.valueOf(number), TextToSpeech.QUEUE_FLUSH, paramsMap);
                    }
                }
                break;
            }
            case TimerService.SOUND_EXERCISE_FINISHED: {
                if (userPrefs.getBoolPref(UserPrefs.VOICE_EXERCISE_NAME, false)) {
                    String name = intent.getStringExtra(TimerService.EXERCISE_NAME);
                    prepareVolume();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(name, TextToSpeech.QUEUE_FLUSH, paramsBundle, null);
                    } else {
                        textToSpeech.speak(name, TextToSpeech.QUEUE_FLUSH, paramsMap);
                    }
                }
                break;
            }
        }
    }

    private void vibrate(Intent intent) {
        if (!userPrefs.getBoolPref(UserPrefs.VIBRATE_ON, false)) {
            return;
        }
        switch (intent.getStringExtra(TimerService.ACTION_TYPE)) {
            case TimerService.SOUND_WORKOUT_FINISHED: {
                if (userPrefs.getBoolPref(UserPrefs.VIBRATE_WORKOUT_FINISH, false)) {
                    long[] pattern = new long[]{0, 300, 300, 300, 300, 300};
                    vibrator.vibrate(pattern, -1);
                }
                break;
            }
            case TimerService.SOUND_EXERCISE_FINISHED: {
                if (userPrefs.getBoolPref(UserPrefs.VIBRATE_EXERCISE_FINISH, false)) {
                    vibrator.vibrate(700);
                }
                break;
            }
            case TimerService.SOUND_HALF: {
                if (userPrefs.getBoolPref(UserPrefs.VIBRATE_HALF, false)) {
                    vibrator.vibrate(500);
                }
                break;
            }
            case TimerService.SOUND_COUNTDOWN: {
                if (userPrefs.getBoolPref(UserPrefs.VIBRATE_COUNTDOWN, false)) {
                    vibrator.vibrate(200);
                }
                break;
            }
        }
    }
}
