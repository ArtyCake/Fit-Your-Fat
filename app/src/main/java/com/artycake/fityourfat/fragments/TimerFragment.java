package com.artycake.fityourfat.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.services.TimerService;
import com.github.lzyzsd.circleprogress.DonutProgress;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimerFragment extends Fragment {

    @BindView(R.id.exercise_name)
    TextView exerciseName;
    @BindView(R.id.timer_text)
    TextView timerText;
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
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
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
                }
            }
        };
        startService();
        IntentFilter filter = new IntentFilter(TimerService.BROADCAST_FILTER);
        getActivity().registerReceiver(receiver, filter);
    }

    private void tick(Intent intent) {
        exerciseName.setText(intent.getStringExtra(TimerService.UE_NAME));
        timerProgress.setProgress(intent.getIntExtra(TimerService.UE_PERCENT, 0));
        timerText.setText(intent.getStringExtra(TimerService.UE_TIME));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
        getActivity().unbindService(connection);
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
        } else {
            setAction(TimerService.ACTION_START_TIMER);
        }
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
}
