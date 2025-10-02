package com.safari.safarims.repository;

import com.safari.safarims.entity.Payment;
import com.safari.safarims.common.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findByTxRef(String txRef);
    List<Payment> findByStatus(PaymentStatus status);

    // Payments whose expiry time has passed
    @Query("SELECT p FROM Payment p JOIN FETCH p.booking b WHERE p.status = :status AND p.expiresAt IS NOT NULL AND p.expiresAt < :now")
    List<Payment> findExpiredPayments(@Param("status") PaymentStatus status, @Param("now") LocalDateTime now);

    // Payments within reminder window (expiresAt between now and windowEnd)
    @Query("SELECT p FROM Payment p JOIN FETCH p.booking b WHERE p.status = :status AND p.expiresAt IS NOT NULL AND p.expiresAt BETWEEN :from AND :to")
    List<Payment> findPaymentsExpiringBetween(@Param("status") PaymentStatus status,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);
}
