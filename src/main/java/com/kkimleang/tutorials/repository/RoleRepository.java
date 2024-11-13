package com.kkimleang.tutorials.repository;

import com.kkimleang.tutorials.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);

    List<Role> findByNameIn(List<String> names);
}
