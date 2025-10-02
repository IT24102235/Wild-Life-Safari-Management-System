package com.safari.safarims.controller;

import com.safari.safarims.dto.booking.BookingRequest;
import com.safari.safarims.dto.booking.BookingResponse;
import com.safari.safarims.common.enums.BookingStatus;
import com.safari.safarims.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create booking", description = "Create a new booking request (Tourist only)")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        try {
            BookingResponse booking = bookingService.createBooking(request);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error creating booking: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update booking", description = "Update booking within edit window (Tourist only)")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long id,
                                                        @Valid @RequestBody BookingRequest request) {
        try {
            BookingResponse booking = bookingService.updateBooking(id, request);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error updating booking {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel booking", description = "Cancel booking within edit window (Tourist only)")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        try {
            bookingService.cancelBooking(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error cancelling booking {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "Get tourist bookings", description = "Get all bookings for the current tourist")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<List<BookingResponse>> getTouristBookings() {
        List<BookingResponse> bookings = bookingService.getTouristBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping
    @Operation(summary = "Get all bookings", description = "Get all bookings (Staff only)")
    @PreAuthorize("hasRole('BOOKING_OFFICER') or hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Get specific booking details")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        try {
            BookingResponse booking = bookingService.getBookingById(id);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error fetching booking {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get bookings by status", description = "Get bookings filtered by status")
    @PreAuthorize("hasRole('BOOKING_OFFICER') or hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatus(@PathVariable BookingStatus status) {
        List<BookingResponse> bookings = bookingService.getBookingsByStatus(List.of(status));
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/{id}/forward-to-crew")
    @Operation(summary = "Forward to crew", description = "Forward booking to crew manager for allocation")
    @PreAuthorize("hasRole('BOOKING_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<Void> forwardToCrew(@PathVariable Long id) {
        try {
            bookingService.forwardToCrew(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error forwarding booking {} to crew: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/set-edit-window")
    @Operation(summary = "Set edit window", description = "Set custom edit window for booking")
    @PreAuthorize("hasRole('BOOKING_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<Void> setEditWindow(@PathVariable Long id, @RequestParam Integer editWindowSeconds) {
        try {
            bookingService.setEditWindow(id, editWindowSeconds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error setting edit window for booking {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/set-payment-window")
    @Operation(summary = "Set payment window", description = "Set custom payment window for booking")
    @PreAuthorize("hasRole('BOOKING_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<Void> setPaymentWindow(@PathVariable Long id, @RequestParam Integer paymentWindowSeconds) {
        try {
            bookingService.setPaymentWindow(id, paymentWindowSeconds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error setting payment window for booking {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
