package com.devprogen.application.user;

import com.devprogen.application.user.record.request.ContactSelectUser;
import com.devprogen.application.user.record.request.UserSignInDTO;
import com.devprogen.application.user.record.request.UserSignUpDTO;
import com.devprogen.application.user.record.request.UserUpdateProfileDTO;
import com.devprogen.domain.user.model.User;
import com.devprogen.domain.user.projection.UserSignUpProjection;
import jakarta.mail.MessagingException;

import java.util.List;

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
    Object countAllByAdmin();
    List<User> searchAllUsers();
    List<User> findAllByIsDeleted(Boolean deleted);
    String recoverDeletedUser(Long idUser);
    String resetSelectedUserPassword(Long idUser) throws MessagingException;
    String contactSelectUser(String userNameOfAdmin,ContactSelectUser contactSelectUser) throws MessagingException;
    String deleteSelectedUser(Long idUser);
}
