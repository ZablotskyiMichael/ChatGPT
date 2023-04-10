package com.quantumquontity.chatgpt.dict;

import com.quantumquontity.chatgpt.R;

import java.util.Arrays;
import java.util.List;

/**
 * Содержит данные о страничках в приложении.
 */
public enum SubPage {

    /**
     * Основная страница.
     */
    MAIN(R.id.mainPageWrapper),

    /**
     * Страница с чатом.
     */
    CHAT(R.id.chatPageWrapper),

    /**
     * Страница покупки подписки.
     */
    SUBSCRIBE(R.id.subscribePageWrapper);

    /**
     * Айдишник основного элемента страницы.
     * Который идет самым первым в xml.
     */
    private final int pageWrapperId;

    SubPage(int pageWrapperId) {
        this.pageWrapperId = pageWrapperId;
    }

    public int getPageWrapperId() {
        return pageWrapperId;
    }

    public static List<SubPage> getAll(){
        return Arrays.asList(SubPage.values());
    }
}
