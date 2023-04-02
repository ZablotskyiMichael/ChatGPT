package com.quantumquontity.chatgpt.service;

import com.quantumquontity.chatgpt.dao.ChatDao;
import com.quantumquontity.chatgpt.data.Chat;

import java.util.List;

public class ChatService {

    private final ChatDao chatDao;

    public ChatService(ChatDao chatDao) {
        this.chatDao = chatDao;
    }

    public List<Chat> getAll(){
        return chatDao.getChatList();
    }

    public void deleteChat(long id){
        chatDao.deleteChat(id);
    }

    public long createNewChat(String name){
        Chat chat = new Chat();
        chat.setName(name);
        return chatDao.save(chat);
    }
}
