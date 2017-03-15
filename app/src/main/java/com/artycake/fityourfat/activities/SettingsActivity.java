package com.artycake.fityourfat.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.fragments.SettingsFragment;
import com.artycake.fityourfat.utils.PermissionsChecker;
import com.artycake.fityourfat.utils.UserPrefs;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        Log.d("TAG key", key);
        if (key.equals(UserPrefs.PAUSE_ON_CALL) && sharedPreferences.getBoolean(UserPrefs.PAUSE_ON_CALL, false)) {
            if (!PermissionsChecker.getInstance(this).checkPhoneStatePermission(new PermissionsChecker.OnPermissionGranted() {
                @Override
                public void onGranted() {
                    sharedPreferences.edit().putBoolean(UserPrefs.PAUSE_ON_CALL, true).apply();
                    getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
                }
            })) {
                sharedPreferences.edit().putBoolean(UserPrefs.PAUSE_ON_CALL, false).apply();
                getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsChecker.getInstance(this).checkResult(requestCode, permissions, grantResults);
    }
}
