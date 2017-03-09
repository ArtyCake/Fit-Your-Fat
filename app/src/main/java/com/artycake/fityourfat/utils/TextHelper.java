package com.artycake.fityourfat.utils;

import java.util.Locale;

/**
 * Created by artycake on 3/8/17.
 */

public class TextHelper {
    public static String formatTime(int time) {
        int minutes = time / 60;
        int seconds = time - (minutes * 60);
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }
}
