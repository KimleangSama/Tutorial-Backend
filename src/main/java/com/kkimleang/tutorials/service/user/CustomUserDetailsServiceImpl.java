package com.kkimleang.tutorials.service.user;

import com.kkimleang.tutorials.entity.User;
import com.kkimleang.tutorials.repository.*;
import java.util.*;
import lombok.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.*;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    //    @Cacheable(value = "CustomUserDetails", key = "#username")
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = Optional.ofNullable(userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username)));
        User u = user.orElse(null);
        return new CustomUserDetails(u, null);
    }
}
