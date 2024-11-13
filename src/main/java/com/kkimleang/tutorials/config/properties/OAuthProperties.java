package com.kkimleang.tutorials.config.properties;

import lombok.*;
import org.springframework.boot.context.properties.*;

@Setter
@Getter
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {
    private String[] authorizedRedirectUris;
}