package com.example.modeltelegrambot.service;

import com.example.modeltelegrambot.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    void addUser(Long userId,String firstName,String userName);
    void setIsUzbek(Long chatId,boolean isUzbek);
    UserEntity getUser(Long userId);
    Page<UserEntity> findAllUsers(int page);
    Long getUsersCountDay();
    Long getUsersCountWeek();
    Long getUsersCountMonth();
    Long getUsersCount();
}
