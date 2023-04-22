package com.quantumquontity.chatgpt.service;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public class GlobalValuesHolder {

    private int dpInPx40 = 100;

    public GlobalValuesHolder(Resources resources) {
        try {
            dpInPx40 = (int) (40 * ((float) resources.getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        } catch (Exception e) {
            // do nothing
        }
    }

    public int get40dpInPx() {
        return dpInPx40;
    }
}
