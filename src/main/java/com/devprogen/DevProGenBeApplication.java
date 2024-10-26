package com.devprogen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;

import java.sql.Date;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class DevProGenBeApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevProGenBeApplication.class, args);
    }
}
