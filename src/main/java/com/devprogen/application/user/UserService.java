package com.devprogen.application.user;

import com.devprogen.application.user.record.request.UserSignInDTO;
import com.devprogen.application.user.record.request.UserSignUpDTO;
import com.devprogen.domain.user.model.User;

public interface UserService {
    Object signInUser(UserSignInDTO userSignInDTO);
    Object signUpUser(UserSignUpDTO userSignUpDTO);

    User loadUserByUsername(String username);
}
