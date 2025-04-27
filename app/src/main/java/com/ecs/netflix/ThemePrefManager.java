package com.ecs.netflix;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemePrefManager {
    private static final String PREF_NAME = "theme_pref";
    private static final String KEY_THEME = "current_theme";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public ThemePrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setDarkMode(boolean isDarkMode) {
        editor.putBoolean(KEY_THEME, isDarkMode);
        editor.apply();
    }

    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_THEME, false); // Default: açık mod
    }
}
