package com.devprogen;

import com.devprogen.domain.enumerations.AccountTypeEnum;
import com.devprogen.domain.user.model.User;
import com.devprogen.domain.user.service.UserDomainService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class DevProGenBeApplication {
    public static void main(String[] args) {
        ApplicationContext ac = SpringApplication.run(DevProGenBeApplication.class, args);
    }
}
