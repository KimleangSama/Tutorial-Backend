package com.kkimleang.tutorials;

import com.redis.om.spring.annotations.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cache.annotation.*;

@SpringBootApplication
@EnableCaching
@EnableRedisEnhancedRepositories(basePackages = {"com.kkimleang.tutorials.repository", "com.kkimleang.tutorials.entity"})
public class TutorialApplication {
    public static void main(String[] args) {
        SpringApplication.run(TutorialApplication.class, args);
    }
}
