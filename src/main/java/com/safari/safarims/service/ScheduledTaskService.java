package com.safari.safarims.service;

import com.safari.safarims.entity.Payment;
import com.safari.safarims.entity.Booking;
import com.safari.safarims.common.enums.PaymentStatus;
import com.safari.safarims.common.enums.BookingStatus;
import com.safari.safarims.repository.PaymentRepository;
import com.safari.safarims.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    @Transactional
    public void checkPaymentExpiry() {
        LocalDateTime now = LocalDateTime.now();
        List<Payment> expired = paymentRepository.findExpiredPayments(PaymentStatus.PENDING, now);
        if (expired.isEmpty()) return;
        for (Payment payment : expired) {
            try {
                paymentService.expirePayment(payment.getId());
                log.info("Expired payment {} for booking {}", payment.getId(), payment.getBooking().getId());
            } catch (Exception e) {
                log.error("Error expiring payment {}: {}", payment.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void sendPaymentReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusMinutes(2); // same 2‑minute lead time as original logic
        List<Payment> expiringSoon = paymentRepository.findPaymentsExpiringBetween(PaymentStatus.PENDING, now, windowEnd);
        if (expiringSoon.isEmpty()) return;

        for (Payment payment : expiringSoon) {
            try {
                Booking booking = payment.getBooking();
                emailService.sendPaymentReminder(
                    booking.getTourist().getUser().getEmail(),
                    booking.getId().toString(),
                    payment.getAmount().toString()
                );
                notificationService.notifyUser(
                    booking.getTourist().getUser().getId(),
                    "PAYMENT",
                    "Payment Reminder",
                    "Your payment for booking #" + booking.getId() + " will expire soon"
                );
                log.info("Payment reminder queued for booking {} (payment {})", booking.getId(), payment.getId());
            } catch (Exception e) {
                log.error("Error sending payment reminder for payment {}: {}", payment.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void cleanupExpiredOtps() {
        try {
            otpService.cleanupExpiredOtps();
            log.info("Cleaned up expired OTPs");
        } catch (Exception e) {
            log.error("Error cleaning up expired OTPs: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void checkForwardedBookings() {
        // Check for bookings that have been forwarded to crew but not allocated within reasonable time
        List<Booking> forwardedBookings = bookingRepository.findByStatus(BookingStatus.FORWARDED_TO_CREW);
        LocalDateTime threshold = LocalDateTime.now().minusHours(24); // 24 hours threshold

        for (Booking booking : forwardedBookings) {
            if (booking.getUpdatedAt().isBefore(threshold)) {
                // Notify crew managers about pending allocation
                notificationService.notifyCrewManagers(
                    "Pending Allocation Reminder",
                    "Booking #" + booking.getId() + " has been pending allocation for over 24 hours"
                );
                log.info("Reminded crew managers about pending booking: {}", booking.getId());
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") // Run daily at 1 AM
    public void generateDailyReport() {
        try {
            long newBookings = bookingRepository.findByStatus(BookingStatus.REQUESTED).size();
            long confirmedBookings = bookingRepository.findByStatus(BookingStatus.CONFIRMED).size();
            long cancelledBookings = bookingRepository.findByStatus(BookingStatus.CANCELLED).size();
            long successfulPayments = paymentRepository.findByStatus(PaymentStatus.SUCCESS).size();
            log.info("Daily Report - New: {}, Confirmed: {}, Cancelled: {}, Payments: {}",
                newBookings, confirmedBookings, cancelledBookings, successfulPayments);
        } catch (Exception e) {
            log.error("Error generating daily report: {}", e.getMessage());
        }
    }
}
