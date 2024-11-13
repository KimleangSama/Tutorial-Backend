package com.kkimleang.tutorials.config.properties;

import lombok.*;
import org.springframework.boot.context.properties.*;

@Getter
@Setter
@AllArgsConstructor
@ConfigurationProperties(prefix = "token")
public class TokenProperties {
    private Integer accessTokenExpiresHours;
    private Integer refreshTokenExpiresHours;
    private String domain;
}