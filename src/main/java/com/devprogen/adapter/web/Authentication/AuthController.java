package com.devprogen.adapter.web.Authentication;

import com.devprogen.adapter.wrapper.ResponseWrapper;
import com.devprogen.application.user.UserService;
import com.devprogen.application.user.record.request.UserSignInDTO;
import com.devprogen.application.user.record.request.UserSignUpDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/SignIn")
    public ResponseEntity<?> SignInUser(@Valid @RequestBody final UserSignInDTO userToSignIn) {
        Object data = userService.signInUser(userToSignIn);
        if(data instanceof String) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseWrapper.error((String) data));

        return ResponseEntity.status(HttpStatus.OK).body(ResponseWrapper.success(data));
    }

    @PostMapping("/SignUp")
    public ResponseEntity<?> SignUpUser(@Valid @RequestBody final UserSignUpDTO userToSignUp) {
        Object data = userService.signUpUser(userToSignUp);
        if(data instanceof String) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseWrapper.error((String) data));

        return ResponseEntity.status(HttpStatus.OK).body(ResponseWrapper.success(data));
    }
}
