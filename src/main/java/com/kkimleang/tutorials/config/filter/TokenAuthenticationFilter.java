package com.kkimleang.tutorials.config.filter;

import com.kkimleang.tutorials.entity.*;
import com.kkimleang.tutorials.repository.*;
import com.kkimleang.tutorials.service.user.*;
import com.kkimleang.tutorials.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.*;
import org.springframework.security.web.authentication.*;
import org.springframework.util.*;
import org.springframework.web.filter.*;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String email;
        try {
            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt) && tokenProvider.isTokenNotExpired(jwt)) {
                email = tokenProvider.getUserEmailFromToken(jwt);
                User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
                if (tokenProvider.isTokenValid(jwt, user)) {
                    CustomUserDetails customUserDetails = new CustomUserDetails(user, null);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            customUserDetails,
                            null,
                            customUserDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
