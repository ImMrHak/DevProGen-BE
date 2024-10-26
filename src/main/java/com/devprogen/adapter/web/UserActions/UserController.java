package com.devprogen.adapter.web.UserActions;

import com.devprogen.adapter.wrapper.ResponseWrapper;
import com.devprogen.application.attribute.AttributeService;
import com.devprogen.application.entity.EntityService;
import com.devprogen.application.log.LogService;
import com.devprogen.application.project.ProjectService;
import com.devprogen.application.user.UserService;
import com.devprogen.domain.user.model.User;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    //private final ProjectService projectService;
    //private final EntityService entityService;
    //private final AttributeService attributeService;
    //private final LogService logService;

    @GetMapping("/userInfo") @PreAuthorize("isAuthenticated() && hasAnyRole('ADMIN')")
    public ResponseEntity<String> getUserInfo(Principal principal) {
        //System.out.println("Principal: {}" +  principal);
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
        return ResponseEntity.ok("Hello, " + principal.getName());
    }



}
