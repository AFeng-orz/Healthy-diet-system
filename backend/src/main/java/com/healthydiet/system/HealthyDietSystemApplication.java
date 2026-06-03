package com.healthydiet.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.healthydiet.system.mapper")
@SpringBootApplication
public class HealthyDietSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthyDietSystemApplication.class, args);
    }
}