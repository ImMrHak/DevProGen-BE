package com.devprogen.application.user;

import com.devprogen.domain.user.model.User;
import com.devprogen.domain.user.service.UserDomainService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserDomainService userDomainService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public User loadUserByUsername(String username) {
        return userDomainService.findByUserName(username);
    }
}