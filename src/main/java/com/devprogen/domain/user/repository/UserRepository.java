package com.devprogen.domain.user.repository;

import com.devprogen.domain.user.model.User;
import com.devprogen.domain.user.projection.UserSignInProjection;
import com.devprogen.domain.user.projection.UserSignUpProjection;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository {
    User findByUserName(String username);

    @Query("SELECT u.idUser AS idUser, u.userName AS userName, u.isAdmin AS isAdmin, u.password AS password, u.isDeleted AS isDeleted " +
            "FROM User u WHERE u.userName = :username")
    UserSignInProjection findProjectedUserByUserNameToSignIn(String username);

    @Query("SELECT u.idUser AS idUser, u.userName AS userName, u.isAdmin AS isAdmin, u.password AS password, u.isDeleted AS isDeleted " +
            "FROM User u WHERE u.email = :email")
    UserSignInProjection findProjectedUserByEmailToSignIn(String email);

    @Query("SELECT u.idUser AS idUser, u.userName AS userName, u.isAdmin AS isAdmin, u.isDeleted AS isDeleted " +
            "FROM User u WHERE u.userName = :userName OR u.email = :email")
    UserSignUpProjection findByUserNameOrEmailToSignUp(String userName, String email);

    Boolean existsByUserNameOrEmail(String userName, String email);
}
