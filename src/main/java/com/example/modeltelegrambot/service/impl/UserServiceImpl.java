package com.example.modeltelegrambot.service.impl;

import com.example.modeltelegrambot.entity.UserEntity;
import com.example.modeltelegrambot.repositories.UserRepository;
import com.example.modeltelegrambot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public void addUser(Long userId, String firstName, String userName) {
        UserEntity user = UserEntity.builder()
                .userName(userName)
                .userId(userId)
                .firstName(firstName)
                .build();
        userRepository.save(user);
    }

    @Override
    public void setIsUzbek(Long chatId, boolean isUzbek) {
        UserEntity user = getUser(chatId);
        user.setUzbek(isUzbek);
        userRepository.save(user);
    }

    @Override
    public UserEntity getUser(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public Page<UserEntity> findAllUsers(int page) {
        return userRepository.findAll(PageRequest.of(page,200));
    }

    @Override
    public Long getUsersCountDay() {
        return userRepository.getUsersCountDay();
    }

    @Override
    public Long getUsersCountWeek() {
        return userRepository.getUsersCountWeek();
    }

    @Override
    public Long getUsersCountMonth() {
        return userRepository.getUsersCountMonth();
    }

    @Override
    public Long getUsersCount() {
        return userRepository.getAllUsersCount();
    }
}
