package com.safari.safarims.repository;

import com.safari.safarims.entity.OutboundEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboundEmailRepository extends JpaRepository<OutboundEmail, Long> {
    List<OutboundEmail> findByToEmailOrderByCreatedAtDesc(String toEmail);
    List<OutboundEmail> findByStatus(String status);
    List<OutboundEmail> findByStatusOrderByCreatedAtDesc(String status);
}
