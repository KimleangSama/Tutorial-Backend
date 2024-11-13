package com.kkimleang.tutorials.payload.response;

import com.kkimleang.tutorials.entity.*;
import java.io.*;
import java.util.*;
import lombok.*;

@Getter
@Setter
@ToString
public class RoleResponse implements Serializable {
    private UUID id;
    private String name;
    private Set<PermissionResponse> permissions;

    public static Set<RoleResponse> fromRoles(Set<Role> roles) {
        return roles.stream().map(role -> {
            RoleResponse roleResponse = new RoleResponse();
            roleResponse.setId(role.getId());
            roleResponse.setName(role.getName());
            roleResponse.setPermissions(PermissionResponse.fromPermissions(role.getPermissions()));
            return roleResponse;
        }).collect(java.util.stream.Collectors.toSet());
    }
}
