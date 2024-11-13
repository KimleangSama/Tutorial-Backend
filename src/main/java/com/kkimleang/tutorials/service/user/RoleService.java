package com.kkimleang.tutorials.service.user;

import com.kkimleang.tutorials.exception.*;
import com.kkimleang.tutorials.entity.*;
import com.kkimleang.tutorials.repository.*;
import jakarta.transaction.*;
import java.util.*;
import lombok.*;
import org.springframework.stereotype.*;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    @Transactional
    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Role", "name", name)
                );
    }

    @Transactional
    public List<Role> findByNames(List<String> names) {
        return roleRepository.findByNameIn(names);
    }
}
