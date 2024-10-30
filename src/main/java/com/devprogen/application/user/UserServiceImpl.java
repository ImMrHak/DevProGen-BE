package com.devprogen.application.user;

import com.devprogen.application.user.mapper.UserMapper;
import com.devprogen.application.user.record.request.ContactSelectUser;
import com.devprogen.application.user.record.request.UserSignInDTO;
import com.devprogen.application.user.record.request.UserSignUpDTO;
import com.devprogen.application.user.record.request.UserUpdateProfileDTO;
import com.devprogen.domain.enumerations.AccountTypeEnum;
import com.devprogen.domain.user.model.User;
import com.devprogen.domain.user.projection.UserSignInProjection;
import com.devprogen.domain.user.projection.UserSignUpProjection;
import com.devprogen.domain.user.service.UserDomainService;
import com.devprogen.infrastructure.config.JWT.JwtUtil;
import com.devprogen.infrastructure.utility.Utility;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDomainService userDomainService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final Utility utility;


    @Override
    public Object userDataInfo(String userName) {
        return userMapper.UserToUserInformationDTO(userDomainService.loadUserByUsername(userName));
    }

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

        // USE MAPPER IS BETTER
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

        // USE MAPPER IS BETTER
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

    @Override
    public Object updateMyProfile(String userName, UserUpdateProfileDTO userUpdateProfileDTO) {
        User dbUser = userDomainService.findByUserName(userName);

        dbUser.setFirstName(userUpdateProfileDTO.firstName());
        dbUser.setLastName(userUpdateProfileDTO.lastName());
        dbUser.setEmail(userUpdateProfileDTO.email());

        return userMapper.UserToUserInformationDTO(userDomainService.save(dbUser));
    }

    @Override
    public Boolean isTokenCorrect(String token) {
        if(token.isEmpty()) return false;
        return jwtUtils.isTokenCorrect(token, userDomainService);
    }

    @Override
    public Object countAllByAdmin() {
        Map<String, Object> data = new HashMap<>();
        data.put("users", userDomainService.countAllByIsAdmin(false));
        data.put("admins", userDomainService.countAllByIsAdmin(true));

        return data;
    }

    @Override
    public List<User> searchAllUsers() {
        return userDomainService.findAll().stream().filter(u -> !u.isDeleted()).toList();
    }

    @Override
    public List<User> findAllByIsDeleted(Boolean deleted) {
        return userDomainService.findAllByIsDeleted(deleted);
    }

    @Override
    public String recoverDeletedUser(Long idUser) {
        User userDbToRecover = userDomainService.findById(idUser);
        if(!userDbToRecover.isDeleted()) return "Account is already active";
        userDbToRecover.setDeleted(false);

        userDomainService.save(userDbToRecover);
        return "Account successfully recovered";
    }

    @Override
    public String resetSelectedUserPassword(Long idUser) throws MessagingException {
        User userDbToResetPassword = userDomainService.findById(idUser);

        if(userDbToResetPassword == null) return "User was not found";

        String rawPassword = Utility.generateRandomAlphanumeric(10);

        userDbToResetPassword.setPassword(passwordEncoder.encode(rawPassword));

        userDomainService.save(userDbToResetPassword);

        String resetPasswordMessage = buildResetPasswordEmailHTML(userDbToResetPassword, rawPassword);

        utility.sendEmail(userDbToResetPassword.getEmail(), "Reset Password Request", resetPasswordMessage, true);
        return "User password has been successfully reset";
    }

    @Override
    public String contactSelectUser(String userNameOfAdmin, ContactSelectUser contactSelectUser) throws MessagingException {
        User userDbToContact = userDomainService.findByEmail(contactSelectUser.email());
        User userDbAdmin = userDomainService.loadUserByUsername(userNameOfAdmin);

        if(userDbToContact == null) return "User was not found";

        String contactMessage = buildCustomMessageEmailHTML(userDbAdmin, userDbToContact, contactSelectUser.message());

        utility.sendEmail(userDbToContact.getEmail(), "Mail From Administrator: " + userDbAdmin.getFirstName() + " " + userDbAdmin.getLastName(), contactMessage, true);
        return "Mail sent successfully";
    }

    @Override
    public String deleteSelectedUser(Long idUser) {
        User userDbToDelete = userDomainService.findById(idUser);

        if(userDbToDelete == null) return "User was not found";

        if(userDbToDelete.isDeleted()) return "User already deleted";

        userDbToDelete.setDeleted(true);

        userDomainService.save(userDbToDelete);

        return "User deleted successfully";
    }

    public String buildCustomMessageEmailHTML(User adminUser, User userToContact, String message) {
        String applicationName = utility.applicationName;
        String imageUrl = utility.applicationLogoUrl;

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Helvetica Neue', Arial, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; }" +
                ".email-wrapper { background-color: #f9f9f9; padding: 20px; }" +
                ".email-content { background-color: #ffffff; border-radius: 12px; padding: 40px; max-width: 600px; margin: auto; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1); }" +
                "h1 { font-size: 24px; color: #333333; margin-bottom: 20px; }" +
                "p { font-size: 16px; line-height: 1.8; color: #555555; margin-bottom: 20px; }" +
                ".highlight { font-weight: bold; color: #007BFF; }" +
                ".message-container { background-color: #f4f4f4; border-radius: 8px; padding: 15px; font-family: monospace; white-space: pre-wrap; color: #333333; }" +
                ".footer { text-align: center; margin-top: 40px; font-size: 13px; color: #999999; }" +
                ".logo { display: block; width: 100%; height: auto; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='email-wrapper'>" +
                "<div class='email-content'>" +
                "<img src='" + imageUrl + "' alt='Logo' class='logo' />" +
                "<p>Dear <span class='highlight'>" + userToContact.getFirstName() + " " + userToContact.getLastName() + "</span>,</p>" +
                "<div class='message-container'>" + message + "</div>" +
                "<p>Message sent by: <span class='highlight'>" + adminUser.getFirstName() + " " + adminUser.getLastName() + "</span> from " + applicationName + ".</p>" +
                "<p>Best regards,</p>" +
                "<p>The " + applicationName + " Team</p>" +
                "<div class='footer'>" +
                "<p>© 2024 " + applicationName + ". All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }



    public String buildResetPasswordEmailHTML(User userToResetPassword, String rawPassword) {
        String applicationName = utility.applicationName;
        String imageUrl = utility.applicationLogoUrl;

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Helvetica Neue', Arial, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; }" +
                ".email-wrapper { background-color: #f9f9f9; padding: 20px; }" +
                ".email-content { background-color: #ffffff; border-radius: 12px; padding: 40px; max-width: 600px; margin: auto; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1); }" +
                "h1 { font-size: 24px; color: #333333; margin-bottom: 20px; }" +
                "p { font-size: 16px; line-height: 1.8; color: #555555; margin-bottom: 20px; }" +
                ".highlight { font-weight: bold; color: #007BFF; }" +
                ".credentials { background-color: #f4f4f4; border-radius: 8px; padding: 20px; margin-bottom: 30px; }" +
                ".credentials p { margin: 0; font-size: 15px; color: #333333; }" +
                ".cta-button { display: inline-block; background-color: #dc3545; color: white; text-decoration: none; font-size: 16px; padding: 12px 25px; border-radius: 6px; margin-top: 30px; }" +
                ".cta-button:hover { background-color: #c82333; }" +
                ".footer { text-align: center; margin-top: 40px; font-size: 13px; color: #999999; }" +
                ".logo { display: block; width: 100%; height: auto; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='email-wrapper'>" +
                "<div class='email-content'>" +
                "<img src='" + imageUrl + "' alt='Logo' class='logo' />" +
                "<h1>Password Reset Request</h1>" +
                "<p>Dear <span class='highlight'>" + userToResetPassword.getFirstName() + " " + userToResetPassword.getLastName() + "</span>,</p>" +
                "<p>We have received a request to reset your password. Below are your temporary login credentials:</p>" +
                "<div class='credentials'>" +
                "<p><strong>Username:</strong> <code>" + userToResetPassword.getUsername() + "</code></p>" +
                "<p><strong>Temporary Password:</strong> <code>" + rawPassword + "</code></p>" +
                "</div>" +
                "<p>For security reasons, please use this temporary password to log in and reset your password immediately.</p>" +
                "<a style='color: white;' href='" + utility.applicationDomainUrl + "/DevProGen/SignIn' class='cta-button'>Reset Your Password</a>" +
                "<p>If you did not request a password reset, please contact our <a href='mailto:" + utility.applicationSupportEmail + "'>support team</a> immediately.</p>" +
                "<div class='footer'>" +
                "<p>Best regards,</p>" +
                "<p>The " + applicationName + " Team</p>" +
                "<p>© 2024 " + applicationName + ". All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

}