package com.artycake.fityourfat.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by artycake on 3/8/17.
 */

public class TextHelper {
    private final static List<Locale> availableLocales = new ArrayList<>(Arrays.asList(new Locale[]{new Locale("ru", "RU")}));

    public static Locale getCurrentLocale() {
        Locale defaultLocale = Locale.getDefault();
        if (availableLocales.contains(defaultLocale)) {
            return defaultLocale;
        }
        return Locale.ENGLISH;
    }

    public static String formatTime(int time) {
        int minutes = time / 60;
        int seconds = time - (minutes * 60);
        return String.format(getCurrentLocale(), "%d:%02d", minutes, seconds);
    }
}
