package com.devprogen.application.user;

import com.devprogen.application.user.record.request.UserSignInDTO;
import com.devprogen.application.user.record.request.UserSignUpDTO;
import com.devprogen.application.user.record.request.UserUpdateProfileDTO;
import com.devprogen.domain.user.model.User;
import com.devprogen.domain.user.projection.UserSignUpProjection;

public interface UserService {
    Object userDataInfo(String userName);
    Object signInUser(UserSignInDTO userSignInDTO);
    Object signUpUser(UserSignUpDTO userSignUpDTO);
    UserSignUpProjection signUpoAuth2User(User user);
    Boolean existsByUserNameOrEmail(String userName, String email);
    UserSignUpProjection findByUserNameOrEmailToSignUp(String userName, String email);
    User loadUserByUsername(String username);
    Object updateMyProfile(String userName, UserUpdateProfileDTO userUpdateProfileDTO);
    Boolean isTokenCorrect(String token);
}
