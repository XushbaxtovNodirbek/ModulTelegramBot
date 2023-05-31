package com.example.modeltelegrambot.service.impl;

import com.example.modeltelegrambot.entity.ChatEntity;
import com.example.modeltelegrambot.repositories.ChatRepository;
import com.example.modeltelegrambot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    @Override
    public void addChat(String title,String userName, long countMembers, Long chatId) {
        ChatEntity entity = ChatEntity.builder()
                .tile(title)
                .membersCount(countMembers)
                .chatId(chatId)
                .userName(userName)
                .build();
        chatRepository.save(entity);
    }

    @Override
    public Page<ChatEntity> getAllChats(int page) {
        return chatRepository.findAll(PageRequest.of(page,200, Sort.by("membersCount").descending()));
    }
    public Page<ChatEntity> getAllChatList(int page) {
        return chatRepository.findAll(PageRequest.of(page,20, Sort.by("membersCount").descending()));
    }


    @Override
    public Long getAllMembersCount() {
        return chatRepository.getAllCountMembers();
    }

    @Override
    public ChatEntity getChat(Long chatId) {
        return chatRepository.findById(chatId).orElse(null);
    }

    @Override
    public Long getCountAllChats() {
        return chatRepository.getCountAllChats();
    }

    public void deleteChat(Long chatID){
        chatRepository.delete(getChat(chatID));
    }
}
