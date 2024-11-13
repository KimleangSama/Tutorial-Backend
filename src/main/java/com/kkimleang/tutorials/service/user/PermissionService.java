package com.kkimleang.tutorials.service.user;

import com.kkimleang.tutorials.entity.*;
import com.kkimleang.tutorials.repository.*;
import lombok.*;
import org.springframework.stereotype.*;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public Permission findByName(String name) {
        return permissionRepository.findByName(name);
    }
}
