package com.artycake.fityourfat.fragments;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.activities.MainActivity;
import com.artycake.fityourfat.activities.SettingsActivity;
import com.artycake.fityourfat.services.TimerService;
import com.artycake.fityourfat.utils.UserPrefs;
import com.github.lzyzsd.circleprogress.DonutProgress;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimerFragment extends Fragment {

    @BindView(R.id.exercise_name)
    TextView exerciseName;
    @BindView(R.id.workout_name)
    TextView workoutName;
    @BindView(R.id.laps)
    TextView laps;
    @BindView(R.id.timer_text)
    TextView timerText;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.timer_progress)
    DonutProgress timerProgress;
    @BindView(R.id.start_stop_btn)
    Button startStopBtn;
    @BindView(R.id.pause_btn)
    Button pauseResumeBtn;

    private boolean bound;
    private ServiceConnection connection;
    private TimerService timerService;
    private BroadcastReceiver receiver;

    public TimerFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                bound = true;
                timerService = ((TimerService.TimerBinder) binder).getService();
                Log.d("TAG", "Bound service");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("TAG", intent.getStringExtra(TimerService.ACTION_TYPE));
                switch (intent.getStringExtra(TimerService.ACTION_TYPE)) {
                    case TimerService.TIMER_STARTED: {
                        startStopBtn.setText(R.string.main_stop_btn);
                        pauseResumeBtn.setVisibility(View.VISIBLE);
                        break;
                    }
                    case TimerService.TIMER_STOPPED: {
                        startStopBtn.setText(R.string.main_start_btn);
                        pauseResumeBtn.setText(R.string.main_pause_btn);
                        pauseResumeBtn.setVisibility(View.GONE);
                        break;
                    }
                    case TimerService.TIMER_PAUSED: {
                        pauseResumeBtn.setText(R.string.main_resume_btn);
                        break;
                    }
                    case TimerService.TIMER_RESUMED: {
                        pauseResumeBtn.setText(R.string.main_pause_btn);
                        break;
                    }
                    case TimerService.UPDATE_EXERCISE: {
                        tick(intent);
                        break;
                    }
                    case TimerService.WORKOUT_CHANGED: {
                        workoutName.setText(intent.getStringExtra(TimerService.WORKOUT_NAME));
                        break;
                    }
                    case TimerService.SERVICE_STOPPED: {
                        getActivity().finishAffinity();
                        break;
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(TimerService.BROADCAST_UI_FILTER);
        getActivity().registerReceiver(receiver, filter);
        startService();
    }

    private void tick(Intent intent) {
        exerciseName.setText(intent.getStringExtra(TimerService.UE_NAME));
        timerProgress.setProgress(intent.getIntExtra(TimerService.UE_PERCENT, 0));
        timerText.setText(intent.getStringExtra(TimerService.UE_TIME));
        description.setText(intent.getStringExtra(TimerService.UE_DESC));
        String lapString = getResources().getString(R.string.laps,
                intent.getIntExtra(TimerService.UE_CURRENT_LAP, 0),
                intent.getIntExtra(TimerService.UE_LAPS, 0));
        laps.setText(lapString);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(connection);
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    private Intent setAction(String action) {
        Intent intent = new Intent(getActivity(), TimerService.class);
        intent.setAction(action);
        getActivity().startService(intent);
        return intent;
    }

    void startService() {
        Intent intent = setAction(TimerService.ACTION_START_SERVICE);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @OnClick(R.id.start_stop_btn)
    void onStartStopClick() {
        if (!bound) {
            return;
        }
        Log.d("FORSERVICE", String.valueOf(timerService.isStarted()));
        if (timerService.isStarted()) {
            setAction(TimerService.ACTION_STOP_TIMER);
            askForRate();
        } else {
            setAction(TimerService.ACTION_START_TIMER);
        }
    }

    private void askForRate() {
        final UserPrefs userPrefs = UserPrefs.getInstance(getContext());
        if (!userPrefs.getBoolPref(UserPrefs.ASK_FOR_RATE, true)) {
            return;
        }
        int stopsFromLast = userPrefs.getIntPref(UserPrefs.STOPS_FROM_LAST, 0);
        stopsFromLast++;
        if (stopsFromLast < UserPrefs.STOPS_FOR_RATE) {
            userPrefs.putPreferences(UserPrefs.STOPS_FROM_LAST, stopsFromLast);
            return;
        }
        userPrefs.putPreferences(UserPrefs.STOPS_FROM_LAST, 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.rate_dialog_title, getResources().getString(R.string.app_name)));
        builder.setMessage(R.string.rate_dialog_message);
        builder.setPositiveButton(R.string.rate_dialog_rate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getActivity().getPackageName())));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.rate_dialog_never, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userPrefs.putPreferences(UserPrefs.ASK_FOR_RATE, false);
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.rate_dialog_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @OnClick(R.id.pause_btn)
    void onPauseResumeClick() {
        if (!bound) {
            return;
        }
        Log.d("FORSERVICE", String.valueOf(timerService.isPaused()));
        if (timerService.isPaused()) {
            setAction(TimerService.ACTION_RESUME_TIMER);
        } else {
            setAction(TimerService.ACTION_PAUSE_TIMER);
        }
    }

    @OnClick(R.id.to_workouts)
    void toWorkouts() {
        ((MainActivity) getActivity()).toWorkouts();
    }

    @OnClick(R.id.action_settings)
    void openSettings() {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
    }
}
