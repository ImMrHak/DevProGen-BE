package com.devprogen.infrastructure.config.oAuth2;

import com.devprogen.application.user.UserService;
import com.devprogen.domain.enumerations.AccountTypeEnum;
import com.devprogen.domain.user.model.User;
import com.devprogen.domain.user.projection.UserSignUpProjection;
import com.devprogen.infrastructure.config.JWT.JwtUtil;
import com.devprogen.infrastructure.utility.Utility;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtils;
    private final UserService userService;
    private final Utility utility;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String fullName = oAuth2User.getAttribute("name");

        String[] nameParts = fullName.split(" ");
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        String userName = firstName + lastName;

        UserSignUpProjection dbUser = userService.existsByUserNameOrEmail(userName, email) ? userService.findByUserNameOrEmailToSignUp(userName, email) : null;
        if (dbUser == null) {
            String provider = AccountTypeEnum.fromString(oauthToken.getAuthorizedClientRegistrationId()).toString();
            String rawPassword = Utility.generateRandomAlphanumeric(10);
            User newUser = User.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .password(rawPassword)
                    .email(email)
                    .userName(userName)
                    .accountType(provider)
                    .build();

            String subject = "Welcome to " + utility.applicationName + "! Your Account Details Inside";
            String message = buildWelcomeEmailHTML(newUser);

            try {
                utility.sendEmail(email, subject, message, true);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }

            dbUser = userService.signUpoAuth2User(newUser);
        }

        if(dbUser.getIsDeleted()){
            getRedirectStrategy().sendRedirect(request, response, utility.applicationDomainUrl + "/DevProGen/SignIn");
        }

        String generatedToken = jwtUtils.generateToken(dbUser.getUserName(), dbUser.getAuthorities());

        // Add the JWT token in the response header
        response.addHeader("Authorization", "Bearer " + generatedToken);
        getRedirectStrategy().sendRedirect(request, response, utility.applicationDomainUrl + "/DevProGen/oauth2/redirect?token=" + generatedToken + "&rid=" + dbUser.getAuthorities().toString().charAt(6));
    }

    public String buildWelcomeEmailHTML(User newUser) {
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
                ".cta-button { display: inline-block; background-color: #28a745; color: white; text-decoration: none; font-size: 16px; padding: 12px 25px; border-radius: 6px; margin-top: 30px; }" +
                ".cta-button:hover { background-color: #218838; }" +
                ".footer { text-align: center; margin-top: 40px; font-size: 13px; color: #999999; }" +
                ".logo { display: block; width: 100%; height: auto; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='email-wrapper'>" +
                "<div class='email-content'>" +
                "<img src='" + imageUrl + "' alt='Logo' class='logo' />" +
                "<h1>Welcome to " + applicationName + ", " + newUser.getFirstName() + "!</h1>" +
                "<p>Dear <span class='highlight'>" + newUser.getFirstName() + " " + newUser.getLastName() + "</span>,</p>" +
                "<p>We're thrilled to welcome you to " + applicationName + " - your one-stop solution for generating back-end and front-end applications effortlessly.</p>" +
                "<p>With " + applicationName + ", you can now create robust back-end APIs using Spring Boot and dynamic front-end interfaces using Angular, all at the click of a button. Say goodbye to manual setup and hello to quick, efficient project generation!</p>" +
                "<p>Here are your login details for accessing your account:</p>" +
                "<div class='credentials'>" +
                "<p><strong>Username:</strong> <code>" + newUser.getUsername() + "</code></p>" +
                "<p><strong>Password:</strong> <code>" + newUser.getPassword() + "</code></p>" +
                "</div>" +
                "<p>For ease, you can also use your Google, Facebook or GitHub account for login. Remember to save your credentials for future reference.</p>" +
                "<a style='color: white;' href='" + utility.applicationDomainUrl + "/DevProGen/SignIn' class='cta-button'>Start Generating Your Application</a>" +
                "<p>If you have any questions or need assistance, please reach out to our <a href='mailto:" + utility.applicationSupportEmail + "'>support team</a>.</p>" +
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
