package com.example.modeltelegrambot.service;

import com.example.modeltelegrambot.entity.ChatEntity;
import org.springframework.data.domain.Page;

public interface ChatService {
    void addChat(String title,String userName,long countMembers,Long chatId);
    Page<ChatEntity> getAllChats(int page);

    Long getAllMembersCount();
    Long getCountAllChats();

    ChatEntity getChat(Long chatId);
}
