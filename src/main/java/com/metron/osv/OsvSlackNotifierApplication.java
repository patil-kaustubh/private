package com.metron.osv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // <<-- This enables scheduled polling
public class OsvSlackNotifierApplication {
    public static void main(String[] args) {
        SpringApplication.run(OsvSlackNotifierApplication.class, args);
    }
}
