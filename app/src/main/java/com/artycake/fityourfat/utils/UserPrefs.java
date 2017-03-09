package com.artycake.fityourfat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

/**
 * Created by artycake on 3/9/17.
 */

public class UserPrefs {
    private static UserPrefs instance;
    private final SharedPreferences preferences;

    public static final String FIRST_LAUNCH = "first_launch";
    public static final String CURRENT_WORKOUT = "current_workout";

    public static UserPrefs getInstance(Context context) {
        if (instance == null) {
            instance = new UserPrefs(context);
        }
        return instance;
    }

    private UserPrefs(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void putPreferences(String name, String value) {
        preferences.edit().putString(name, value).apply();
    }

    public void putPreferences(String name, int value) {
        preferences.edit().putInt(name, value).apply();
    }

    public void putPreferences(String name, long value) {
        preferences.edit().putLong(name, value).apply();
    }

    public void putPreferences(String name, boolean value) {
        preferences.edit().putBoolean(name, value).apply();
    }

    public String getStringPref(String name, @Nullable String defaultValue) {
        return preferences.getString(name, defaultValue);
    }

    public int getIntPref(String name, int defaultValue) {
        return preferences.getInt(name, defaultValue);
    }

    public long getLongPref(String name, long defaultValue) {
        return preferences.getLong(name, defaultValue);
    }

    public boolean getBoolPref(String name, boolean defaultValue) {
        return preferences.getBoolean(name, defaultValue);
    }

    public void addOnPreferenceChange(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void removeOnPreferenceChange(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
