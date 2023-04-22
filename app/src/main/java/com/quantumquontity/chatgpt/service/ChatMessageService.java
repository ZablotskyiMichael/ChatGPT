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

    public void updateChatMessageText(long id, String newText){
        chatMessageDao.updateChatMessageText(id, newText);
    }

    public ChatMessage save(ChatMessage message){
        long id = chatMessageDao.save(message);
        message.setId(id);
        return message;
    }

    public void deleteChatMessage(long id){
        chatMessageDao.deleteChatMessage(id);
    }
}
