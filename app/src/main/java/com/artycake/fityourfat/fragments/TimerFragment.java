package com.artycake.fityourfat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.services.TimerService;

public class TimerFragment extends Fragment {

    private boolean on = false;

    public TimerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        TextView textView = (TextView) view.findViewById(R.id.timer_text);
        textView.setText("16:98");
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FORSERVICE", "clicked");
                Intent intent = new Intent(getContext(), TimerService.class);
                if (on) {
                    intent.setAction(TimerService.ACTION_STOP_SERVICE);
                } else {
                    intent.setAction(TimerService.ACTION_START_SERVICE);
                }
                getContext().startService(intent);
                on = !on;
            }
        });
        return view;
    }
}
