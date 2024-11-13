package com.kkimleang.tutorials.payload.response;

import java.io.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
public class AuthResponse implements Serializable {
    private final String accessToken;
    private final String refreshToken;
    private String tokenType = "Bearer";
    private final String username;
    private final Instant expiredAt;
}