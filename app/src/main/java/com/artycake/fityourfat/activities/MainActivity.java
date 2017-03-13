package com.artycake.fityourfat.activities;

import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.WindowManager;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.adapters.MainFragmentsAdapter;
import com.artycake.fityourfat.utils.UserPrefs;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.container)
    ViewPager viewPager;

    private MainFragmentsAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        sectionsPagerAdapter = new MainFragmentsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(1);
    }

    public void toWorkouts() {
        viewPager.setCurrentItem(0, true);
    }

    public void toTimer() {
        viewPager.setCurrentItem(1, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UserPrefs.getInstance(this).getBoolPref(UserPrefs.KEEP_SCREEN_ON, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
