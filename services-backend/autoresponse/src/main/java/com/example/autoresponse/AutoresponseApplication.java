package com.example.autoresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AutoresponseApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoresponseApplication.class, args);
    }
}