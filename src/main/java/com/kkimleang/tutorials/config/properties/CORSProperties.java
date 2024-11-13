package com.kkimleang.tutorials.config.properties;

import lombok.*;
import org.springframework.boot.context.properties.*;

@Setter
@Getter
@ConfigurationProperties(prefix = "cors")
public class CORSProperties {
    private String[] allowedOrigins;
}
