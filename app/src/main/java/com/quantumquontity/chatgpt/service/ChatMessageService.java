package com.quantumquontity.chatgpt.service;

import com.quantumquontity.chatgpt.dao.ChatMessageDao;
import com.quantumquontity.chatgpt.data.ChatMessage;

import java.util.List;

public class ChatMessageService {

    private final ChatMessageDao chatMessageDao;

    public ChatMessageService(ChatMessageDao chatMessageDao) {
        this.chatMessageDao = chatMessageDao;
    }

    public List<ChatMessage> getChatMessagesList(long chatId){
        return chatMessageDao.getChatMessagesList(chatId);
    }
}
