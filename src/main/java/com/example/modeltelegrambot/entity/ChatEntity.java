package com.example.modeltelegrambot.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
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
    @Column(columnDefinition = "varchar(255) default 'Mavjud Emas'")
    String userName;
    @Column(columnDefinition = "bigint default 0")
    Long membersCount;
    @CreationTimestamp
    Date createdAt;

    @Override
    public String toString() {
        return "Guruh nomi : " + tile + "\n" +
                " userName : " + (userName==null?"mavjud emas":"@"+userName) + "\n" +
                " azolar soni : " + membersCount +"\n";
    }
}
