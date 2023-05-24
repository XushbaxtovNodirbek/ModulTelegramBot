package com.example.modeltelegrambot.service;

import com.example.modeltelegrambot.entity.ChatEntity;
import org.springframework.data.domain.Page;

public interface ChatService {
    void addChat(String title,long countMembers,Long chatId);
    Page<ChatEntity> getAllChats(int page);

    long getAllMembersCount(Long chatId);

    ChatEntity getChat(Long chatId);
}
