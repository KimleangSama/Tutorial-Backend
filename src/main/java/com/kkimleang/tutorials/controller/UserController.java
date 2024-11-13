package com.kkimleang.tutorials.controller;

import com.kkimleang.tutorials.annotation.*;
import com.kkimleang.tutorials.entity.*;
import com.kkimleang.tutorials.payload.*;
import com.kkimleang.tutorials.payload.request.*;
import com.kkimleang.tutorials.payload.response.*;
import com.kkimleang.tutorials.service.user.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public Response<UserResponse> getCurrentUser(@CurrentUser CustomUserDetails currentUser) {
        try {
            User user = userService.findByEmail(currentUser.getEmail());
            UserResponse userResponse = UserResponse.fromUser(user);
            log.info("User: {}", userResponse);
            return Response.<UserResponse>ok()
                    .setPayload(userResponse);
        } catch (Exception e) {
            return Response.<UserResponse>exception()
                    .setErrors(e.getMessage());
        }
    }

    @GetMapping("/{id}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public Response<UserResponse> getUserProfile(@PathVariable UUID id) {
        try {
            Optional<User> user = userService.findById(id);
            if (user.isEmpty()) {
                return Response.<UserResponse>notFound()
                        .setErrors("User with id " + id + " not found");
            }
            UserResponse userResponse = UserResponse.fromUser(user.get());
            return Response.<UserResponse>ok()
                    .setPayload(userResponse);
        } catch (Exception e) {
            return Response.<UserResponse>exception()
                    .setErrors(e.getMessage());
        }
    }

    @PostMapping("/profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public Response<List<UserResponse>> getUserProfiles(@RequestBody List<UUID> ids) {
        try {
            List<User> users = userService.findByIds(ids);
            List<UserResponse> userResponses = UserResponse.fromUsers(users);
            return Response.<List<UserResponse>>ok()
                    .setPayload(userResponses);
        } catch (Exception e) {
            return Response.<List<UserResponse>>exception()
                    .setErrors(e.getMessage());
        }
    }

    @PutMapping("/update/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') and hasAuthority('UPDATE')")
    public Response<UserResponse> updateUserProfile(
            @CurrentUser CustomUserDetails currentUser,
            @RequestBody UpdateProfileRequest request) {
        try {
            User user = userService.findByEmail(currentUser.getEmail());
            if (!user.getId().equals(request.getId())) {
                return Response.<UserResponse>accessDenied()
                        .setErrors("You are not allowed to update this user profile");
            }
            user.setProfileURL(request.getProfileURL());
            User updatedUser = userService.updateUserProfile(user);
            UserResponse userResponse = UserResponse.fromUser(updatedUser);
            return Response.<UserResponse>ok()
                    .setPayload(userResponse);
        } catch (Exception e) {
            return Response.<UserResponse>exception()
                    .setErrors(e.getMessage());
        }
    }

    @PutMapping("/{id}/delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('DELETE')")
    public Response<UserResponse> deleteUser(@PathVariable UUID id) {
        try {
            User user = userService.updateIsDeletedToUser(id);
            if (user != null) {
                UserResponse userResponse = UserResponse.fromUser(user);
                return Response.<UserResponse>ok()
                        .setPayload(userResponse);
            } else {
                return Response.<UserResponse>exception()
                        .setErrors("Cannot delete user");
            }
        } catch (Exception e) {
            return Response.<UserResponse>exception()
                    .setErrors(e.getMessage());
        }
    }
}
