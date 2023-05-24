package com.example.modeltelegrambot.entity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;


@Data
@Entity(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEntity implements Serializable {
    @Id
    Long userId;
    String firstName;
    String userName;
    boolean isUzbek;
    @CreationTimestamp
    Date createdAt;
}
