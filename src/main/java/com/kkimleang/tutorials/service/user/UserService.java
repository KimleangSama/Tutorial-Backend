package com.kkimleang.tutorials.service.user;

import com.fasterxml.jackson.databind.*;
import com.kkimleang.tutorials.entity.User;
import com.kkimleang.tutorials.entity.*;
import com.kkimleang.tutorials.enumeration.*;
import com.kkimleang.tutorials.exception.*;
import com.kkimleang.tutorials.payload.request.*;
import com.kkimleang.tutorials.payload.response.*;
import com.kkimleang.tutorials.repository.*;
import com.kkimleang.tutorials.util.*;
import jakarta.servlet.http.*;
import jakarta.transaction.*;
import jakarta.validation.constraints.*;
import java.io.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.amqp.rabbit.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final OTPService OTPService;
    private final DeviceManagementRepository deviceManagementRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final RabbitTemplate rabbitTemplate;
    @Value("${rabbitmq.exchange.email.name}")
    private String emailExchange;
    @Value("${rabbitmq.binding.email.name}")
    private String emailRoutingKey;

    @Cacheable(value = "user", key = "#email")
    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with " + email + " not found."));
        return filterIsDeleted(user);
    }

    public boolean isExistingByEmail(@NotBlank @Email String email) {
        return userRepository.existsByEmail(email);
    }

    public User createUser(SignUpRequest signUpRequest) {
        try {
            User user = new User();
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(signUpRequest.getPassword());
            // Setup roles following the request, if empty role, set default role to ROLE_USER
            if (signUpRequest.getRoles().isEmpty()) {
                Role userRole = roleService.findByName("ROLE_USER");
                user.getRoles().add(userRole);
            } else {
                signUpRequest.getRoles().forEach(role -> {
                    try {
                        List<Role> roles = roleService.findByNames(List.of(role));
                        user.getRoles().addAll(roles);
                    } catch (ResourceNotFoundException e) {
                        log.error("Role not found: {} with message: {}", role, e.getMessage(), e);
                    }
                });
            }
            user.setProvider(AuthProvider.LOCAL);
            user.setIsEnabled(true);
            user.setIsVerified(false);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User signupUser = userRepository.save(user);
            String randomCode = RandomString.make(24);
            OTP code = OTPService.save(signupUser, randomCode);
            if (code == null) {
                throw new RuntimeException("Cannot create OTP code for user with email: " + signUpRequest.getEmail());
            }
            return signupUser;
        } catch (Exception e) {
            log.error("Cannot create user with email: {}", signUpRequest.getEmail(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public AuthResponse loginUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            if (authentication == null) {
                throw new BadCredentialsException("Username or password is incorrect.");
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = tokenProvider.createAccessToken(authentication);
            String refreshToken = tokenProvider.createRefreshToken(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            if (user == null) {
                throw new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail());
            }
            DeviceManagement device = getDeviceManagement(loginRequest, user);
            deviceManagementRepository.save(device);
            return new AuthResponse(
                    accessToken,
                    refreshToken,
                    user.getUsername(),
                    tokenProvider.getExpirationDateFromToken(accessToken)
            );
        } catch (Exception e) {
            String message = "We cannot authenticate user. Please check email and password.";
            log.error(message);
            throw new BadCredentialsException(message);
        }
    }

    private static DeviceManagement getDeviceManagement(LoginRequest loginRequest, User user) {
        DeviceManagement device = new DeviceManagement();
        device.setUserAgent(loginRequest.getDeviceManagement().getUserAgent());
        device.setDeviceOS(loginRequest.getDeviceManagement().getDeviceOS());
        device.setDeviceVersion(loginRequest.getDeviceManagement().getDeviceVersion());
        device.setLocation(loginRequest.getDeviceManagement().getLocation());
        device.setLoginTime(loginRequest.getDeviceManagement().getLoginTime());
        device.setUser(user);
        return device;
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = tokenProvider.getUserEmailFromToken(refreshToken);
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
            if (tokenProvider.isTokenValid(refreshToken, user)) {
                var accessToken = tokenProvider.createAccessToken(user);
                var authResponse = new AuthResponse(
                        accessToken,
                        refreshToken,
                        user.getUsername(),
                        tokenProvider.getExpirationDateFromToken(accessToken)
                );
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public Optional<User> findById(UUID id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "Id", id));
            return Optional.of(filterIsDeleted(user));
        } catch (Exception e) {
            throw new RuntimeException("Error while finding user with id: " + id + " with message: " + e.getMessage());
        }
    }

    public List<User> findByIds(List<UUID> ids) {
        try {
            List<User> users = userRepository.findByIdIn(ids);
            if (users.isEmpty()) {
                throw new ResourceNotFoundException("User", "Ids", ids);
            }
            return filterIsDeleted(users);
        } catch (Exception e) {
            throw new RuntimeException("Error while finding users with ids: " + ids + " with message: " + e.getMessage());
        }
    }

    @CachePut(value = "user", key = "#user.email")
    @Transactional
    public User updateUserProfile(User user) {
        try {
            Integer updated = userRepository.updateUserProfile(user.getId(), user.getProfileURL());
            if (updated == 1) {
                return user;
            } else {
                throw new RuntimeException("Cannot update user profile with id: " + user.getId());
            }
        } catch (Exception e) {
            log.error("Exception Error: {}", e.getMessage());
            throw new ResourceNotFoundException("User", "id", user.getId());
        }
    }

    @CachePut(value = "user", key = "#user.email")
    @Transactional
    public User updateIsVerified(User user, Boolean isVerified) {
        try {
            Integer success = userRepository.updateVerification(user.getId(), isVerified);
            if (success == 1) {
                log.info("User with id: {} is verified: {}", user.getId(), isVerified);
                return user;
            } else {
                throw new RuntimeException("Cannot update user verification with id: " + user.getId());
            }
        } catch (Exception e) {
            log.error("Cannot save user with id: {}", user.getId());
            throw new RuntimeException("User with id: " + user.getId() + " cannot be saved.");
        }
    }

    @CachePut(value = "user", key = "#id")
    public User updateIsDeletedToUser(UUID id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
            user.setIsDeleted(true);
            return userRepository.save(user);
        } catch (Exception e) {
            log.error("Cannot delete user with id: {}", id);
            throw new RuntimeException("User with id: " + id + " cannot be deleted.");
        }
    }

    private List<User> filterIsDeleted(List<User> users) {
        return users.stream()
                .filter(user -> !user.getIsDeleted())
                .toList();
    }

    private User filterIsDeleted(User user) {
        if (user.getIsDeleted()) {
            throw new ResourceNotFoundException("User", "Id", user.getId());
        }
        return user;
    }
}
