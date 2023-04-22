package com.quantumquontity.chatgpt.service;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;

import com.quantumquontity.chatgpt.MainActivity;

public class PointService {

    private final static String SHARED_PREFERENCES_POINTS = "SHARED_PREFERENCES_POINTS";
    private final static String SHARED_PREF_POINTS_VALUE = "SHARED_PREF_POINTS_VALUE";
    private final static int DEF_POINTS_VALUE = 15;

    private final MainActivity mainActivity;

    public PointService(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public int getCurrentPoints() {
        SharedPreferences prefSettingsConfig = mainActivity.getSharedPreferences(SHARED_PREFERENCES_POINTS, MODE_PRIVATE);
        return prefSettingsConfig.getInt(SHARED_PREF_POINTS_VALUE, DEF_POINTS_VALUE);
    }

    public void updatePoints(int newValue) {
        SharedPreferences prefSettingsConfig = mainActivity.getSharedPreferences(SHARED_PREFERENCES_POINTS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefSettingsConfig.edit();
        editor.putInt(SHARED_PREF_POINTS_VALUE, newValue);
        editor.apply();
    }
}
