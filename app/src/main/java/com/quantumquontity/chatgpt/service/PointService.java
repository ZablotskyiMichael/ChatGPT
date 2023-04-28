package com.quantumquontity.chatgpt.service;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.view.View;

import com.quantumquontity.chatgpt.MainActivity;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class PointService {

    private final static String SHARED_PREFERENCES_POINTS = "SHARED_PREFERENCES_POINTS";
    private final static String SHARED_PREF_POINTS_VALUE = "SHARED_PREF_POINTS_VALUE";
    private final static String SHARED_PREF_TIMER_VALUE = "SHARED_PREF_TIMER_VALUE";
    private final static int DEF_POINTS_VALUE = 10;
    public final static int MAX_POINTS_VALUE = 10;
    public final static long RESPAWN_EVERY_HOURS = 4;

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final MainActivity mainActivity;

    CountDownTimer timer;

    public PointService(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        addPointsByTimer();
    }

    private void addPointsByTimer() {
        if (mainActivity.isInfinityPoints()) {
            // Если у пользователя премиум - просто выходим от сюда
            return;
        }
        int currentPoints = getCurrentPoints();
        if (currentPoints < MAX_POINTS_VALUE) {
            Instant lastProcessedTime = Instant.ofEpochMilli(getCurrentTimer());
            Instant now = Instant.now();
            long hours = Duration.between(lastProcessedTime, now).toHours();
            long pointsToAdd = hours / RESPAWN_EVERY_HOURS;
            if (pointsToAdd > 0) {
                updatePointsSharedPref((int) Math.min(currentPoints + pointsToAdd, MAX_POINTS_VALUE));
            }
            if (getCurrentPoints() < MAX_POINTS_VALUE) {
                Instant plus = lastProcessedTime.plus(pointsToAdd * RESPAWN_EVERY_HOURS, ChronoUnit.HOURS);

                SharedPreferences prefSettingsConfig = mainActivity.getSharedPreferences(SHARED_PREFERENCES_POINTS, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefSettingsConfig.edit();

                Instant newLastProcessedTime = plus.isBefore(Instant.now())
                        ? plus : Instant.now();
                editor.putLong(SHARED_PREF_TIMER_VALUE, newLastProcessedTime.toEpochMilli());
                editor.apply();

                initTimer(newLastProcessedTime);
            } else {
                SharedPreferences prefSettingsConfig = mainActivity.getSharedPreferences(SHARED_PREFERENCES_POINTS, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefSettingsConfig.edit();
                editor.remove(SHARED_PREF_TIMER_VALUE);
                editor.apply();
            }
        } else {
            mainActivity.getRequestRespawnTimerTextViews()
                    .forEach(v -> v.setVisibility(View.GONE));
        }
    }

    private void initTimer(Instant newLastProcessedTime) {
        long diffInMillis = Duration.between(Instant.now(), newLastProcessedTime.plus(RESPAWN_EVERY_HOURS, ChronoUnit.HOURS)).toMillis();
        mainActivity.getRequestRespawnTimerTextViews()
                .forEach(v -> v.setVisibility(View.VISIBLE));
        timer = new CountDownTimer(diffInMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                LocalTime timeOfDay = LocalTime.ofNanoOfDay(TimeUnit.MILLISECONDS.toNanos(millisUntilFinished));
                String formattedTime = timeOfDay.format(DATE_TIME_FORMATTER);
                mainActivity.getRequestRespawnTimerTextViews()
                        .forEach(v -> v.setText(formattedTime));
            }

            public void onFinish() {
                mainActivity.getRequestRespawnTimerTextViews()
                        .forEach(v -> v.setVisibility(View.GONE));
                addPointsByTimer();
                mainActivity.setCurrentPointsOnUI();
            }
        }.start();
    }

    public int getCurrentPoints() {
        SharedPreferences prefSettingsConfig = mainActivity.getSharedPreferences(SHARED_PREFERENCES_POINTS, MODE_PRIVATE);
        return prefSettingsConfig.getInt(SHARED_PREF_POINTS_VALUE, DEF_POINTS_VALUE);
    }

    public long getCurrentTimer() {
        SharedPreferences prefSettingsConfig = mainActivity.getSharedPreferences(SHARED_PREFERENCES_POINTS, MODE_PRIVATE);
        return prefSettingsConfig.getLong(SHARED_PREF_TIMER_VALUE, Instant.now().toEpochMilli());
    }

    public void updatePoints(int newValue) {
        updatePointsSharedPref(newValue);
        addPointsByTimer();
    }

    private void updatePointsSharedPref(int newValue) {
        SharedPreferences prefSettingsConfig = mainActivity.getSharedPreferences(SHARED_PREFERENCES_POINTS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefSettingsConfig.edit();
        editor.putInt(SHARED_PREF_POINTS_VALUE, newValue);
        editor.apply();
    }
}
