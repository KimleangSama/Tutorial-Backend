package com.kkimleang.tutorials.entity;

import jakarta.persistence.*;
import java.io.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "tb_otps")
public class OTP extends BaseEntityAudit {
    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private User user;

    private String code;
    private Boolean isUsed = false;
    private Instant expiryDate;
}
