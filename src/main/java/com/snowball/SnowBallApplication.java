package com.snowball;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SnowBallApplication {
    public static void main(String[] args) {
        SpringApplication.run(SnowBallApplication.class, args);
    }
}
