package com.quantumquontity.chatgpt.dict;

import com.quantumquontity.chatgpt.R;

public enum Suggests {
    SUGGEST_01(R.string.suggest_01),
    SUGGEST_02(R.string.suggest_02),
    SUGGEST_03(R.string.suggest_03),
    SUGGEST_04(R.string.suggest_04),
    SUGGEST_05(R.string.suggest_05),
    SUGGEST_06(R.string.suggest_06),
    SUGGEST_07(R.string.suggest_07),
    SUGGEST_08(R.string.suggest_08),
    SUGGEST_09(R.string.suggest_09),
    SUGGEST_10(R.string.suggest_10),
    SUGGEST_11(R.string.suggest_11),
    SUGGEST_12(R.string.suggest_12),
    SUGGEST_13(R.string.suggest_13),
    SUGGEST_14(R.string.suggest_14),
    SUGGEST_15(R.string.suggest_15);

    public int getText() {
        return text;
    }

    private int text;

    Suggests(int text) {
        this.text = text;
    }
}
