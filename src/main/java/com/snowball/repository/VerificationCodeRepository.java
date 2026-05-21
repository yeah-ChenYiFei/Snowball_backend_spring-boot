package com.snowball.repository;

import com.snowball.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByUserIdAndCodeAndTypeAndUsedFalse(Long userId, String code, String type);
}
