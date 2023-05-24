package com.example.modeltelegrambot.repositories;

import com.example.modeltelegrambot.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
        Page<UserEntity> findAll(Pageable pageable);

        @Query(value = "SELECT COUNT(*) " +
                " FROM users " +
                " WHERE current_date - date_trunc('day', created_at) < INTERVAL '1 day'",nativeQuery = true)
        Long getUsersCountDay();
        @Query(value = "SELECT COUNT(*) " +
                " FROM users " +
                " WHERE current_date - date_trunc('day', created_at) < INTERVAL '7 day'",nativeQuery = true)
        Long getUsersCountWeek();

        @Query(value = "SELECT COUNT(*) " +
                " FROM users " +
                " WHERE current_date - date_trunc('day', created_at) < INTERVAL '30 day'",nativeQuery = true)
        Long getUsersCountMonth();

        @Query(value = "select count (*) from users ",nativeQuery = true)
        Long getAllUsersCount();
}
