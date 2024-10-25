package com.devprogen.application.user;

import com.devprogen.domain.user.model.User;

public interface UserService {
    User loadUserByUsername(String username);
}
