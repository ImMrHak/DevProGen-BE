package com.devprogen;

import com.devprogen.domain.enumerations.AccountTypeEnum;
import com.devprogen.domain.user.model.User;
import com.devprogen.domain.user.service.UserDomainService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class DevProGenBeApplication {
    public static void main(String[] args) {
        ApplicationContext ac = SpringApplication.run(DevProGenBeApplication.class, args);
        UserDomainService userDomainService = ac.getBean(UserDomainService.class);
        PasswordEncoder passwordEncoder = ac.getBean(PasswordEncoder.class);
        userDomainService.save(User.builder()
                .firstName("Mohamed")
                .lastName("Hakkou")
                .userName("hakkoumohamed23")
                .email("hakkoumohamed23@gmail.com")
                .password(passwordEncoder.encode("test"))
                .accountType(AccountTypeEnum.REGULAR.toString())
                .isAdmin(true)
                .isDeleted(false)
                .build());

        userDomainService.save(User.builder()
                .firstName("PrenomTEST")
                .lastName("NomTEST")
                .userName("test23")
                .email("test23@gmail.com")
                .password(passwordEncoder.encode("test"))
                .accountType(AccountTypeEnum.REGULAR.toString())
                .isAdmin(false)
                .isDeleted(false)
                .build());
    }
}
