package com.think9.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.think9")
public class Think9Application {
    public static void main(String[] args) {
        SpringApplication.run(Think9Application.class, args);
    }
}