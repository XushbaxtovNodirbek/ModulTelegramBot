package com.example.modeltelegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ModelTelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelTelegramBotApplication.class, args);
    }

}
