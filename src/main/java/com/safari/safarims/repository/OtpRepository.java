package com.safari.safarims.repository;

import com.safari.safarims.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmailAndTypeAndUsedAtIsNullAndExpiresAtAfter(
        String email, String type, LocalDateTime now);

    List<Otp> findByEmailAndType(String email, String type);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
