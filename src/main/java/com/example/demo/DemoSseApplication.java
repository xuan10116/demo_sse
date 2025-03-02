package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // 添加此注解以启用Spring Boot功能
public class DemoSseApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoSseApplication.class, args);
    }

}