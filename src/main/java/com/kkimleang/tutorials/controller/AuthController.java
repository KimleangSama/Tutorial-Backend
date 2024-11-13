package com.kkimleang.tutorials.controller;

import com.kkimleang.tutorials.entity.User;
import com.kkimleang.tutorials.exception.*;
import com.kkimleang.tutorials.payload.*;
import com.kkimleang.tutorials.payload.request.*;
import com.kkimleang.tutorials.payload.response.*;
import com.kkimleang.tutorials.service.user.*;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import java.io.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private UserService userService;
    private OTPService OTPService;

    @PostMapping("/login")
    public Response<AuthResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse response = userService.loginUser(loginRequest);
            return Response.<AuthResponse>ok().setPayload(response);
        } catch (UsernameNotFoundException e) {
            return Response.<AuthResponse>wrongCredentials()
                    .setErrors("User with email " + loginRequest.getEmail() + " not found.")
                    .setPayload(null);
        } catch (BadCredentialsException e) {
            return Response.<AuthResponse>wrongCredentials()
                    .setErrors(e.getMessage())
                    .setPayload(null);
        } catch (Exception e) {
            return Response.<AuthResponse>exception()
                    .setErrors("User authentication failed. " + e.getMessage())
                    .setPayload(null);
        }
    }

    @PostMapping("/register")
    public Response<UserResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            if (userService.isExistingByEmail(signUpRequest.getEmail())) {
                return Response.<UserResponse>badRequest()
                        .setErrors("Email is already taken!")
                        .setPayload(null);
            }
            User user = userService.createUser(signUpRequest);
            return Response.<UserResponse>created().setPayload(UserResponse.fromUser(user));
        } catch (Exception e) {
            log.error("User registration failed. Reason: {}", e.getMessage());
            return Response.<UserResponse>badRequest()
                    .setErrors("User registration failed. Reason: " + e.getMessage())
                    .setPayload(null);
        }
    }

    @GetMapping("/verify")
    public Response<UserResponse> verifyUser(
            @RequestParam("code") String verificationCode
    ) {
        try {
            User user = OTPService.verifyOTPAndReturnUser(verificationCode);
            if (user != null) {
                user = userService.updateIsVerified(user, true);
                return Response.<UserResponse>ok()
                        .setPayload(UserResponse.fromUser(user));
            } else {
                return Response.<UserResponse>badRequest()
                        .setErrors("User verification failed.")
                        .setPayload(null);
            }
        } catch (ResourceNotFoundException e) {
            return Response.<UserResponse>exception()
                    .setErrors("Verification code may have already been used or expired.")
                    .setPayload(null);
        } catch (Exception e) {
            return Response.<UserResponse>exception()
                    .setErrors("User verification failed. " + e.getMessage())
                    .setPayload(null);
        }
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        userService.refreshToken(request, response);
    }
}