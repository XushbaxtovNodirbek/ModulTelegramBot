package com.example.modeltelegrambot.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
@Data
@Entity(name = "chats")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatEntity implements Serializable {
    @Id
    Long chatId;
    String tile;
    Long membersCount;
    @CreationTimestamp
    Date createdAt;
}
