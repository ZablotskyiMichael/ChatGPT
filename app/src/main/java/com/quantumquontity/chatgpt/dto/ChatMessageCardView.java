package com.quantumquontity.chatgpt.dto;

import com.quantumquontity.chatgpt.data.ChatMessage;

public class ChatMessageCardView {
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

    public ChatMessageCardView() {
    }

    public ChatMessageCardView(ChatMessage chatMessage) {
        this.id = chatMessage.getId();
        this.chatId = chatMessage.getChatId();
        this.text = chatMessage.getText();
        this.userRole = chatMessage.getUserRole();
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
