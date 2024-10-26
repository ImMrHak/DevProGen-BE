package com.devprogen.application.user;

import com.devprogen.application.user.record.request.UserSignInDTO;
import com.devprogen.application.user.record.request.UserSignUpDTO;
import com.devprogen.domain.enumerations.AccountTypeEnum;
import com.devprogen.domain.user.model.User;
import com.devprogen.domain.user.projection.UserSignInProjection;
import com.devprogen.domain.user.projection.UserSignUpProjection;
import com.devprogen.domain.user.service.UserDomainService;
import com.devprogen.infrastructure.config.JWT.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDomainService userDomainService;
    private final JwtUtil jwtUtils;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Object signInUser(UserSignInDTO userSignInDTO) {
        UserSignInProjection dbUser = (userSignInDTO.usernameOrEmail().contains("@") ?
                userDomainService.findProjectedUserByEmailToSignIn(userSignInDTO.usernameOrEmail()) :
                userDomainService.findProjectedUserByUserNameToSignIn(userSignInDTO.usernameOrEmail())
        );

        if(dbUser == null){
            return "Invalid email or username. Please try again.";
        }

        if(dbUser.getIsDeleted()){
            return "This account has been deactivated. Please contact support if you believe this is a mistake.";
        }

        if(!passwordEncoder.matches(userSignInDTO.password(), dbUser.getPassword())){
            return "The password you entered is incorrect. Please try again or reset your password.";
        }

        Map<String, Object> data = new HashMap<>();

        data.put("token", jwtUtils.generateToken(dbUser.getUserName(), dbUser.getAuthorities()));
        data.put("rid", dbUser.getAuthorities().toString().charAt(6));
        return data;
    }

    @Override
    public Object signUpUser(UserSignUpDTO userSignUpDTO) {
        if (userDomainService.existsByUserNameOrEmail(userSignUpDTO.userName(), userSignUpDTO.Email())) {
            return "Username or email already exists";
        }

        User newUser = User.builder()
                .firstName(userSignUpDTO.firstName())
                .lastName(userSignUpDTO.lastName())
                .password(passwordEncoder.encode(userSignUpDTO.password()))
                .email(userSignUpDTO.Email())
                .userName(userSignUpDTO.userName())
                .accountType(AccountTypeEnum.REGULAR.toString())
                .isAdmin(false)
                .isDeleted(false)
                .creationDate(new Date(System.currentTimeMillis()))
                .build();

        userDomainService.save(newUser);

        Map<String, Object> data = new HashMap<>();

        data.put("token", jwtUtils.generateToken(newUser.getUsername(), newUser.getAuthorities()));
        data.put("rid", newUser.getAuthorities().toString().charAt(6));
        return data;
    }

    @Override
    public UserSignUpProjection signUpoAuth2User(User user){
        if (userDomainService.existsByUserNameOrEmail(user.getUsername(), user.getEmail())) {
            return null;
        }

        User newUser = User.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .password(passwordEncoder.encode(user.getPassword()))
                .email(user.getEmail())
                .userName(user.getUsername())
                .accountType(user.getAccountType())
                .isAdmin(false)
                .isDeleted(false)
                .creationDate(new Date(System.currentTimeMillis()))
                .build();

        userDomainService.save(newUser);

        return userDomainService.findByUserNameOrEmailToSignUp(newUser.getUsername(), newUser.getEmail());
    }

    @Override
    public Boolean existsByUserNameOrEmail(String userName, String email){
        return userDomainService.existsByUserNameOrEmail(userName, email);
    }

    @Override
    public UserSignUpProjection findByUserNameOrEmailToSignUp(String userName, String email){
        return userDomainService.findByUserNameOrEmailToSignUp(userName, email);
    }

    @Override
    public User loadUserByUsername(String username) {
        return userDomainService.findByUserName(username);
    }
}