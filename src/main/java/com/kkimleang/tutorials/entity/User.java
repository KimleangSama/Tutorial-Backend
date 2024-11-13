package com.kkimleang.tutorials.entity;

import com.kkimleang.tutorials.enumeration.*;
import com.redis.om.spring.annotations.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.*;
import java.time.*;
import java.util.*;
import lombok.*;
import org.springframework.data.redis.core.*;

@RedisHash("User")
@Getter
@Setter
@ToString
@Entity
@Table(name = "tb_users", uniqueConstraints = {@UniqueConstraint(columnNames = {"email"}, name = "unq_email")})
public class User extends BaseEntityAudit {
    @Serial
    private static final long serialVersionUID = 1L;

    private String firstname;
    private String lastname;
    private String username;
    private String password;

    @NotNull
    @Indexed
    private String email;

    @Column(name = "profile_url")
    private String profileURL;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "is_enabled")
    private Boolean isEnabled;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "last_login")
    private Instant lastLogin;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "tb_users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "role_id"}, name = "unq_user_role")}
    )
    private Set<Role> roles = new HashSet<>();
}
