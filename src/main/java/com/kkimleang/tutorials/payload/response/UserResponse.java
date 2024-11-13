package com.kkimleang.tutorials.payload.response;

import com.kkimleang.tutorials.entity.*;
import com.kkimleang.tutorials.enumeration.*;
import java.io.*;
import java.util.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse implements Serializable {
    private UUID id;
    private String username;
    private String email;
    private String profileURL;
    private AuthProvider provider;
    private Set<RoleResponse> roles;
    private Boolean isVerified = false;

    public static UserResponse fromUser(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setProfileURL(user.getProfileURL());
        userResponse.setIsVerified(user.getIsVerified());
        userResponse.setProvider(user.getProvider());
        userResponse.setRoles(RoleResponse.fromRoles(user.getRoles()));
        return userResponse;
    }

    public static List<UserResponse> fromUsers(List<User> users) {
        return users.stream()
                .map(UserResponse::fromUser)
                .toList();
    }
}
