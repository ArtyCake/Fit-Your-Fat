package com.artycake.fityourfat.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.artycake.fityourfat.fragments.TimerFragment;

/**
 * Created by artycake on 3/7/17.
 */

public class MainFragmentsAdapter extends FragmentPagerAdapter {
    public MainFragmentsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new TimerFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 1;
    }
}
