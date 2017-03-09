package com.artycake.fityourfat.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.artycake.fityourfat.fragments.TimerFragment;
import com.artycake.fityourfat.fragments.WorkoutsFragment;

/**
 * Created by artycake on 3/7/17.
 */

public class MainFragmentsAdapter extends FragmentPagerAdapter {
    public MainFragmentsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            return new TimerFragment();
        }
        return new WorkoutsFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }
}
