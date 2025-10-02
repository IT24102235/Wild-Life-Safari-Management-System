package com.safari.safarims.repository;

import com.safari.safarims.entity.Payment;
import com.safari.safarims.common.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findByTxRef(String txRef);
    List<Payment> findByStatus(PaymentStatus status);
}
