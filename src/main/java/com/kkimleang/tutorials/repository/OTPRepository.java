package com.kkimleang.tutorials.repository;

import com.kkimleang.tutorials.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface OTPRepository extends JpaRepository<OTP, UUID> {
    Optional<OTP> findByCode(String code);
}
