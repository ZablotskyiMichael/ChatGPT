package com.quantumquontity.chatgpt.data;

/**
 * Сообщение в чате. От пользователя или ChatGPT.
 */
public class ChatMessage {

    private long id;

    /**
     * id чата, в котором находится это сообщение
     */
    private long chatId;

    /**
     * Текст сообщения
     */
    private String text;

    /**
     * {@link com.theokanning.openai.completion.chat.ChatMessageRole}.
     */
    private String userRole;

    public ChatMessage() {
    }

    public ChatMessage(long id, long chatId, String text, String userRole) {
        this.id = id;
        this.chatId = chatId;
        this.text = text;
        this.userRole = userRole;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
