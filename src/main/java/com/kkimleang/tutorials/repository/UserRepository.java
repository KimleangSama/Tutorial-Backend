package com.kkimleang.tutorials.repository;

import com.kkimleang.tutorials.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByIdIn(List<UUID> ids);
    @Modifying
    @Query("UPDATE User u SET u.profileURL = :profileURL WHERE u.id = :id")
    Integer updateUserProfile(UUID id, String profileURL);
    @Modifying
    @Query("UPDATE User u SET u.isVerified = :isVerified WHERE u.id = :id")
    Integer updateVerification(UUID id, Boolean isVerified);
}
