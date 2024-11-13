package com.kkimleang.tutorials.entity;

import jakarta.persistence.*;
import java.io.*;
import lombok.*;
import org.springframework.security.core.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "tb_permissions", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"}, name = "unq_name")})
public class Permission extends BaseEntityAudit implements GrantedAuthority {
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    public Permission() {
    }

    @Override
    public String getAuthority() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Permission permission = (Permission) obj;
        return name.equals(permission.name);
    }
}
