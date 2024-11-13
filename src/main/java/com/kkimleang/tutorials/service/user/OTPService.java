package com.kkimleang.tutorials.service.user;

import com.kkimleang.tutorials.entity.*;
import com.kkimleang.tutorials.exception.*;
import com.kkimleang.tutorials.repository.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OTPService {
    private final OTPRepository OTPRepository;

    public OTP save(User user, String code) {
        OTP OTP = new OTP();
        OTP.setCode(code);
        OTP.setUser(user);
        return OTPRepository.save(OTP);
    }

    public User verifyOTPAndReturnUser(String code) {
        Optional<OTP> opt = OTPRepository.findByCode(code);
        if (opt.isEmpty()) {
            throw new ResourceNotFoundException("OTP", "Code", code);
        }
        OTP codeEntity = opt.get();
        codeEntity.setIsUsed(true);
        OTPRepository.save(codeEntity);
        return codeEntity.getUser();
    }
}
