package com.safari.safarims.service;

import com.safari.safarims.dto.payment.PaymentRequest;
import com.safari.safarims.dto.payment.PaymentResponse;
import com.safari.safarims.entity.Payment;
import com.safari.safarims.entity.Booking;
import com.safari.safarims.common.enums.PaymentStatus;
import com.safari.safarims.common.enums.BookingStatus;
import com.safari.safarims.repository.PaymentRepository;
import com.safari.safarims.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // Validate booking
        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMATION_SENT &&
            booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Booking is not ready for payment");
        }

        // Check if payment already exists
        List<Payment> existingPayments = paymentRepository.findByBookingId(request.getBookingId());
        Payment existingPayment = existingPayments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.PENDING || p.getStatus() == PaymentStatus.SUCCESS)
            .findFirst()
            .orElse(null);

        if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment already completed for this booking");
        }

        // Create or update payment
        Payment payment;
        if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.PENDING) {
            payment = existingPayment;
        } else {
            payment = Payment.builder()
                .booking(booking)
                .amount(request.getAmount() != null ? request.getAmount() : booking.getTotalAmount())
                .method(request.getMethod() != null ? request.getMethod() : "MOCK_PAYMENT")
                .status(PaymentStatus.PENDING)
                .txRef(generateTransactionReference())
                .expiresAt(LocalDateTime.now().plusSeconds(booking.getPaymentWindowSeconds()))
                .build();
        }

        // Mock payment processing
        boolean paymentSuccess = mockPaymentProcessing(request);

        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());

            // Update booking status
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // Send confirmation notifications
            emailService.sendBookingConfirmation(
                booking.getTourist().getUser().getEmail(),
                buildBookingConfirmationDetails(booking)
            );

            notificationService.notifyUser(
                booking.getTourist().getUser().getId(),
                "BOOKING",
                "Payment Successful",
                "Your payment for booking #" + booking.getId() + " has been processed successfully"
            );

            notificationService.notifyBookingOfficers(
                "Payment Received",
                "Payment received for booking #" + booking.getId()
            );

            log.info("Payment processed successfully for booking: {}", booking.getId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);

            notificationService.notifyUser(
                booking.getTourist().getUser().getId(),
                "BOOKING",
                "Payment Failed",
                "Payment for booking #" + booking.getId() + " has failed. Please try again."
            );

            log.warn("Payment failed for booking: {}", booking.getId());
        }

        Payment saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }

    @Transactional
    public void expirePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return; // Already processed
        }

        payment.setStatus(PaymentStatus.EXPIRED);
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);

        // Send expiry notifications
        emailService.sendPaymentExpiry(
            booking.getTourist().getUser().getEmail(),
            booking.getId().toString()
        );

        notificationService.notifyUser(
            booking.getTourist().getUser().getId(),
            "BOOKING",
            "Payment Expired",
            "Payment window has expired for booking #" + booking.getId()
        );

        notificationService.notifyCrewManagers(
            "Booking Expired",
            "Booking #" + booking.getId() + " has expired due to payment timeout"
        );

        log.info("Payment expired for booking: {}", booking.getId());
    }

    public List<PaymentResponse> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        return mapToResponse(payment);
    }

    public PaymentResponse getPaymentByTxRef(String txRef) {
        Payment payment = paymentRepository.findByTxRef(txRef)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        return mapToResponse(payment);
    }

    public List<PaymentResponse> getPendingPayments() {
        return paymentRepository.findByStatus(PaymentStatus.PENDING).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private boolean mockPaymentProcessing(PaymentRequest request) {
        // Mock payment logic - randomly succeed/fail based on card number
        if (request.getCardNumber() != null) {
            // Cards ending in even numbers succeed, odd numbers fail
            String lastDigit = request.getCardNumber().substring(request.getCardNumber().length() - 1);
            int digit = Integer.parseInt(lastDigit);
            return digit % 2 == 0;
        }

        // Default to random success (80% success rate)
        Random random = new Random();
        return random.nextDouble() < 0.8;
    }

    private String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String buildBookingConfirmationDetails(Booking booking) {
        return String.format("""
            Booking ID: %d
            Package: %s
            Date: %s
            Time: %s
            Amount: $%.2f
            Status: CONFIRMED
            """,
            booking.getId(),
            booking.getTourPackage().getName(),
            booking.getRequestedDate(),
            booking.getRequestedTime(),
            booking.getTotalAmount()
        );
    }

    private PaymentResponse mapToResponse(Payment payment) {
        boolean isExpired = payment.getExpiresAt() != null &&
                           LocalDateTime.now().isAfter(payment.getExpiresAt()) &&
                           payment.getStatus() == PaymentStatus.PENDING;

        return PaymentResponse.builder()
            .id(payment.getId())
            .bookingId(payment.getBooking().getId())
            .touristName(payment.getBooking().getTourist().getFullName())
            .packageName(payment.getBooking().getTourPackage().getName())
            .amount(payment.getAmount())
            .status(payment.getStatus())
            .method(payment.getMethod())
            .txRef(payment.getTxRef())
            .paidAt(payment.getPaidAt())
            .expiresAt(payment.getExpiresAt())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .isExpired(isExpired)
            .build();
    }
}
