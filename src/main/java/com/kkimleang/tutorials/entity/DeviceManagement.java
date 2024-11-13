package com.kkimleang.tutorials.entity;

import jakarta.persistence.*;
import java.io.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "tb_device_managements")
public class DeviceManagement extends BaseEntityAudit {
    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private User user;

    @Column(name = "user_agent")
    private String userAgent;
    @Column(name = "device_os")
    private String deviceOS;
    @Column(name = "device_version")
    private String deviceVersion;
    @Column(name = "location")
    private String location;
    @Column(name = "login_time")
    private Instant loginTime;
}
