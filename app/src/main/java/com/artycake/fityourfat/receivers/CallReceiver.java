package com.artycake.fityourfat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.artycake.fityourfat.services.TimerService;
import com.artycake.fityourfat.utils.UserPrefs;

import java.util.Date;

public class CallReceiver extends BroadcastReceiver {

    private static final String OUTGOING_CALL_ACTION = "android.intent.action.NEW_OUTGOING_CALL";
    private boolean wasPaused = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!UserPrefs.getInstance(context).getBoolPref(UserPrefs.PAUSE_ON_CALL, false)) {
            return;
        }
        if (intent.getAction().equals(OUTGOING_CALL_ACTION)) {
            pauseTimer(context);
        } else {
            String state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                resumeTimer(context);
            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state) || TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                pauseTimer(context);
            }
        }
    }

    private void pauseTimer(Context context) {
        Intent intent = new Intent(context, TimerService.class);
        intent.setAction(TimerService.ACTION_PAUSE_TIMER);
        context.startService(intent);
        wasPaused = true;
    }

    private void resumeTimer(Context context) {
        if (!wasPaused) {
            return;
        }
        Intent intent = new Intent(context, TimerService.class);
        intent.setAction(TimerService.ACTION_RESUME_TIMER);
        context.startService(intent);
    }
}
