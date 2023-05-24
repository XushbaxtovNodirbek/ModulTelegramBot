package com.example.modeltelegrambot.repositories;

import com.example.modeltelegrambot.entity.ChatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity,Long> {
    Page<ChatEntity> findAll(Pageable pageable);
    @Query(value = "select sum(members_count) from chats",nativeQuery = true)
    Long getAllCountMembers();
}
