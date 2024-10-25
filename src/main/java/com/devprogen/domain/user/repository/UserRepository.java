package com.devprogen.domain.user.repository;

import com.devprogen.domain.user.model.User;

public interface UserRepository {
    User findByUserName(String username);
}
