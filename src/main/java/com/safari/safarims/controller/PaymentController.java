package com.safari.safarims.controller;

import com.safari.safarims.dto.payment.PaymentRequest;
import com.safari.safarims.dto.payment.PaymentResponse;
import com.safari.safarims.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{bookingId}/pay")
    @Operation(summary = "Process payment", description = "Process payment for a booking (mock implementation)")
    @PreAuthorize("hasRole('TOURIST') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable Long bookingId,
                                                         @RequestBody PaymentRequest request) {
        try {
            request.setBookingId(bookingId);
            PaymentResponse payment = paymentService.processPayment(request);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error processing payment for booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieve specific payment details")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        try {
            PaymentResponse payment = paymentService.getPaymentById(id);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error fetching payment {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payments by booking", description = "Retrieve all payments for a booking")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByBooking(@PathVariable Long bookingId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByBooking(bookingId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/txref/{txRef}")
    @Operation(summary = "Get payment by transaction reference", description = "Retrieve payment by transaction reference")
    public ResponseEntity<PaymentResponse> getPaymentByTxRef(@PathVariable String txRef) {
        try {
            PaymentResponse payment = paymentService.getPaymentByTxRef(txRef);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error fetching payment with txRef {}: {}", txRef, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending payments", description = "Retrieve all pending payments")
    @PreAuthorize("hasRole('BOOKING_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getPendingPayments() {
        List<PaymentResponse> payments = paymentService.getPendingPayments();
        return ResponseEntity.ok(payments);
    }
}
