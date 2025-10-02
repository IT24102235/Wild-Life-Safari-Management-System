package com.safari.safarims.service;

import com.safari.safarims.dto.booking.BookingRequest;
import com.safari.safarims.dto.booking.BookingResponse;
import com.safari.safarims.entity.Booking;
import com.safari.safarims.entity.Tourist;
import com.safari.safarims.entity.TourPackage;
import com.safari.safarims.common.enums.BookingStatus;
import com.safari.safarims.repository.BookingRepository;
import com.safari.safarims.repository.TouristRepository;
import com.safari.safarims.repository.TourPackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TouristRepository touristRepository;
    private final TourPackageRepository tourPackageRepository;
    private final NotificationService notificationService;

    @Value("${app.default-timers.edit-window-seconds}")
    private int defaultEditWindowSeconds;

    @Value("${app.default-timers.payment-window-seconds}")
    private int defaultPaymentWindowSeconds;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Find tourist by current user
        Tourist tourist = touristRepository.findByUserUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Tourist profile not found"));

        // Validate tour package
        TourPackage tourPackage = tourPackageRepository.findById(request.getPackageId())
            .orElseThrow(() -> new RuntimeException("Tour package not found"));

        if (!tourPackage.getIsActive()) {
            throw new RuntimeException("Tour package is not available");
        }

        // Create booking
        Booking booking = Booking.builder()
            .tourist(tourist)
            .tourPackage(tourPackage)
            .requestedDate(request.getRequestedDate())
            .requestedTime(request.getRequestedTime())
            .status(BookingStatus.REQUESTED)
            .editWindowSeconds(defaultEditWindowSeconds)
            .paymentWindowSeconds(defaultPaymentWindowSeconds)
            .totalAmount(tourPackage.getPrice())
            .notes(request.getNotes())
            .build();

        booking.setCreatedBy(currentUsername);
        booking.setUpdatedBy(currentUsername);

        Booking saved = bookingRepository.save(booking);

        // Send notification to booking officers
        notificationService.notifyBookingOfficers("New Booking Request",
            "New booking request #" + saved.getId() + " from " + tourist.getFullName());

        log.info("Booking created: {} by tourist: {}", saved.getId(), tourist.getFullName());

        return mapToResponse(saved);
    }

    @Transactional
    public BookingResponse updateBooking(Long bookingId, BookingRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Check if tourist owns this booking
        if (!booking.getTourist().getUser().getUsername().equals(currentUsername)) {
            throw new RuntimeException("You can only edit your own bookings");
        }

        // Check if booking is still in edit window
        if (!isWithinEditWindow(booking)) {
            throw new RuntimeException("Edit window has expired for this booking");
        }

        // Only allow editing if status is REQUESTED
        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new RuntimeException("Booking cannot be edited in current status: " + booking.getStatus());
        }

        // Validate new tour package
        TourPackage tourPackage = tourPackageRepository.findById(request.getPackageId())
            .orElseThrow(() -> new RuntimeException("Tour package not found"));

        if (!tourPackage.getIsActive()) {
            throw new RuntimeException("Tour package is not available");
        }

        // Update booking
        booking.setTourPackage(tourPackage);
        booking.setRequestedDate(request.getRequestedDate());
        booking.setRequestedTime(request.getRequestedTime());
        booking.setTotalAmount(tourPackage.getPrice());
        booking.setNotes(request.getNotes());
        booking.setVersion(booking.getVersion() + 1);
        booking.setUpdatedBy(currentUsername);

        Booking updated = bookingRepository.save(booking);

        log.info("Booking updated: {} by tourist: {}", updated.getId(), currentUsername);

        return mapToResponse(updated);
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Check if tourist owns this booking
        if (!booking.getTourist().getUser().getUsername().equals(currentUsername)) {
            throw new RuntimeException("You can only cancel your own bookings");
        }

        // Check if booking can be cancelled
        if (booking.getStatus() == BookingStatus.CONFIRMED ||
            booking.getStatus() == BookingStatus.CANCELLED ||
            booking.getStatus() == BookingStatus.EXPIRED) {
            throw new RuntimeException("Booking cannot be cancelled in current status: " + booking.getStatus());
        }

        // For REQUESTED status, check edit window
        if (booking.getStatus() == BookingStatus.REQUESTED && !isWithinEditWindow(booking)) {
            throw new RuntimeException("Edit window has expired for this booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedBy(currentUsername);
        bookingRepository.save(booking);

        log.info("Booking cancelled: {} by tourist: {}", booking.getId(), currentUsername);
    }

    public List<BookingResponse> getTouristBookings() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Tourist tourist = touristRepository.findByUserUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Tourist profile not found"));

        return bookingRepository.findByTouristIdOrderByCreatedAtDesc(tourist.getId()).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<BookingResponse> getBookingsByStatus(List<BookingStatus> statuses) {
        return bookingRepository.findByStatusIn(statuses).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        return mapToResponse(booking);
    }

    @Transactional
    public void forwardToCrew(Long bookingId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new RuntimeException("Only REQUESTED bookings can be forwarded to crew");
        }

        booking.setStatus(BookingStatus.FORWARDED_TO_CREW);
        booking.setUpdatedBy(currentUsername);
        bookingRepository.save(booking);

        // Notify crew managers
        notificationService.notifyCrewManagers("Booking Forwarded",
            "Booking #" + booking.getId() + " has been forwarded for allocation");

        log.info("Booking {} forwarded to crew by {}", bookingId, currentUsername);
    }

    @Transactional
    public void setEditWindow(Long bookingId, Integer editWindowSeconds) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setEditWindowSeconds(editWindowSeconds);
        booking.setUpdatedBy(currentUsername);
        bookingRepository.save(booking);

        log.info("Edit window set to {} seconds for booking {} by {}",
            editWindowSeconds, bookingId, currentUsername);
    }

    @Transactional
    public void setPaymentWindow(Long bookingId, Integer paymentWindowSeconds) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setPaymentWindowSeconds(paymentWindowSeconds);
        booking.setUpdatedBy(currentUsername);
        bookingRepository.save(booking);

        log.info("Payment window set to {} seconds for booking {} by {}",
            paymentWindowSeconds, bookingId, currentUsername);
    }

    private boolean isWithinEditWindow(Booking booking) {
        LocalDateTime editWindowExpiry = booking.getCreatedAt().plusSeconds(booking.getEditWindowSeconds());
        return LocalDateTime.now().isBefore(editWindowExpiry);
    }

    private LocalDateTime getEditWindowExpiry(Booking booking) {
        return booking.getCreatedAt().plusSeconds(booking.getEditWindowSeconds());
    }

    private BookingResponse mapToResponse(Booking booking) {
        boolean canEdit = booking.getStatus() == BookingStatus.REQUESTED && isWithinEditWindow(booking);
        LocalDateTime editWindowExpires = getEditWindowExpiry(booking);

        return BookingResponse.builder()
            .id(booking.getId())
            .touristId(booking.getTourist().getId())
            .touristName(booking.getTourist().getFullName())
            .packageId(booking.getTourPackage().getId())
            .packageName(booking.getTourPackage().getName())
            .packageDays(booking.getTourPackage().getDays())
            .packageMaxPeople(booking.getTourPackage().getMaxPeople())
            .requestedDate(booking.getRequestedDate())
            .requestedTime(booking.getRequestedTime())
            .status(booking.getStatus())
            .editWindowSeconds(booking.getEditWindowSeconds())
            .paymentWindowSeconds(booking.getPaymentWindowSeconds())
            .version(booking.getVersion())
            .totalAmount(booking.getTotalAmount())
            .notes(booking.getNotes())
            .createdAt(booking.getCreatedAt())
            .updatedAt(booking.getUpdatedAt())
            .createdBy(booking.getCreatedBy())
            .updatedBy(booking.getUpdatedBy())
            .canEdit(canEdit)
            .editWindowExpires(editWindowExpires)
            .build();
    }
}
