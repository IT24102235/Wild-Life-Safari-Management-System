package com.safari.safarims.service;

import com.safari.safarims.dto.allocation.AllocationRequest;
import com.safari.safarims.dto.allocation.AllocationResponse;
import com.safari.safarims.entity.*;
import com.safari.safarims.common.enums.AllocationStatus;
import com.safari.safarims.common.enums.BookingStatus;
import com.safari.safarims.common.enums.JeepStatus;
import com.safari.safarims.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final BookingRepository bookingRepository;
    private final DriverRepository driverRepository;
    private final GuideRepository guideRepository;
    private final JeepRepository jeepRepository;
    private final NotificationService notificationService;

    @Transactional
    public AllocationResponse createAllocation(AllocationRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Validate booking
        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.FORWARDED_TO_CREW) {
            throw new RuntimeException("Booking must be in FORWARDED_TO_CREW status for allocation");
        }

        // Check if allocation already exists
        Optional<Allocation> existingAllocation = allocationRepository.findByBookingId(request.getBookingId());
        if (existingAllocation.isPresent()) {
            throw new RuntimeException("Allocation already exists for this booking");
        }

        // Validate and get resources
        Driver driver = null;
        if (request.getDriverId() != null) {
            driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

            // Check if driver is available
            List<Allocation> activeDriverAllocations = allocationRepository.findActiveAllocationsByDriverId(request.getDriverId());
            if (!activeDriverAllocations.isEmpty()) {
                throw new RuntimeException("Driver is already allocated to another booking");
            }
        }

        Guide guide = null;
        if (request.getGuideId() != null) {
            guide = guideRepository.findById(request.getGuideId())
                .orElseThrow(() -> new RuntimeException("Guide not found"));

            // Check if guide is available
            List<Allocation> activeGuideAllocations = allocationRepository.findActiveAllocationsByGuideId(request.getGuideId());
            if (!activeGuideAllocations.isEmpty()) {
                throw new RuntimeException("Guide is already allocated to another booking");
            }
        }

        Jeep jeep = null;
        if (request.getJeepId() != null) {
            jeep = jeepRepository.findById(request.getJeepId())
                .orElseThrow(() -> new RuntimeException("Jeep not found"));

            if (jeep.getStatus() != JeepStatus.AVAILABLE) {
                throw new RuntimeException("Jeep is not available for allocation");
            }

            // Check if jeep is already allocated
            List<Allocation> activeJeepAllocations = allocationRepository.findActiveAllocationsByJeepId(request.getJeepId());
            if (!activeJeepAllocations.isEmpty()) {
                throw new RuntimeException("Jeep is already allocated to another booking");
            }
        }

        // Create allocation
        Allocation allocation = Allocation.builder()
            .booking(booking)
            .driver(driver)
            .guide(guide)
            .jeep(jeep)
            .status(AllocationStatus.ACTIVE)
            .notes(request.getNotes())
            .build();

        allocation.setCreatedBy(currentUsername);
        allocation.setUpdatedBy(currentUsername);

        Allocation saved = allocationRepository.save(allocation);

        // Update booking status
        booking.setStatus(BookingStatus.ALLOCATED);
        booking.setUpdatedBy(currentUsername);
        bookingRepository.save(booking);

        // Update jeep status
        if (jeep != null) {
            jeep.setStatus(JeepStatus.ALLOCATED);
            jeepRepository.save(jeep);
        }

        // Send notifications
        notificationService.notifyBookingOfficers("Allocation Created",
            "Booking #" + booking.getId() + " has been allocated");

        if (driver != null) {
            notificationService.notifyUser(driver.getUser().getId(), "ALLOCATION",
                "New Assignment", "You have been assigned to booking #" + booking.getId());
        }

        if (guide != null) {
            notificationService.notifyUser(guide.getUser().getId(), "ALLOCATION",
                "New Assignment", "You have been assigned to booking #" + booking.getId());
        }

        log.info("Allocation created: {} for booking: {} by {}", saved.getId(), booking.getId(), currentUsername);

        return mapToResponse(saved);
    }

    @Transactional
    public AllocationResponse updateAllocation(Long allocationId, AllocationRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Allocation allocation = allocationRepository.findById(allocationId)
            .orElseThrow(() -> new RuntimeException("Allocation not found"));

        if (allocation.getStatus() != AllocationStatus.ACTIVE) {
            throw new RuntimeException("Only ACTIVE allocations can be updated");
        }

        // Release previous resources
        if (allocation.getJeep() != null) {
            allocation.getJeep().setStatus(JeepStatus.AVAILABLE);
            jeepRepository.save(allocation.getJeep());
        }

        // Validate and assign new resources (similar validation as in create)
        Driver driver = null;
        if (request.getDriverId() != null) {
            driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

            List<Allocation> activeDriverAllocations = allocationRepository.findActiveAllocationsByDriverId(request.getDriverId());
            activeDriverAllocations = activeDriverAllocations.stream()
                .filter(a -> !a.getId().equals(allocationId))
                .collect(Collectors.toList());

            if (!activeDriverAllocations.isEmpty()) {
                throw new RuntimeException("Driver is already allocated to another booking");
            }
        }

        Guide guide = null;
        if (request.getGuideId() != null) {
            guide = guideRepository.findById(request.getGuideId())
                .orElseThrow(() -> new RuntimeException("Guide not found"));

            List<Allocation> activeGuideAllocations = allocationRepository.findActiveAllocationsByGuideId(request.getGuideId());
            activeGuideAllocations = activeGuideAllocations.stream()
                .filter(a -> !a.getId().equals(allocationId))
                .collect(Collectors.toList());

            if (!activeGuideAllocations.isEmpty()) {
                throw new RuntimeException("Guide is already allocated to another booking");
            }
        }

        Jeep jeep = null;
        if (request.getJeepId() != null) {
            jeep = jeepRepository.findById(request.getJeepId())
                .orElseThrow(() -> new RuntimeException("Jeep not found"));

            if (jeep.getStatus() != JeepStatus.AVAILABLE) {
                throw new RuntimeException("Jeep is not available for allocation");
            }

            List<Allocation> activeJeepAllocations = allocationRepository.findActiveAllocationsByJeepId(request.getJeepId());
            activeJeepAllocations = activeJeepAllocations.stream()
                .filter(a -> !a.getId().equals(allocationId))
                .collect(Collectors.toList());

            if (!activeJeepAllocations.isEmpty()) {
                throw new RuntimeException("Jeep is already allocated to another booking");
            }
        }

        // Update allocation
        allocation.setDriver(driver);
        allocation.setGuide(guide);
        allocation.setJeep(jeep);
        allocation.setNotes(request.getNotes());
        allocation.setUpdatedBy(currentUsername);

        Allocation updated = allocationRepository.save(allocation);

        // Update new jeep status
        if (jeep != null) {
            jeep.setStatus(JeepStatus.ALLOCATED);
            jeepRepository.save(jeep);
        }

        log.info("Allocation updated: {} by {}", updated.getId(), currentUsername);

        return mapToResponse(updated);
    }

    @Transactional
    public void cancelAllocation(Long allocationId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Allocation allocation = allocationRepository.findById(allocationId)
            .orElseThrow(() -> new RuntimeException("Allocation not found"));

        if (allocation.getStatus() != AllocationStatus.ACTIVE) {
            throw new RuntimeException("Only ACTIVE allocations can be cancelled");
        }

        // Release resources
        if (allocation.getJeep() != null) {
            allocation.getJeep().setStatus(JeepStatus.AVAILABLE);
            jeepRepository.save(allocation.getJeep());
        }

        allocation.setStatus(AllocationStatus.CANCELLED);
        allocation.setUpdatedBy(currentUsername);
        allocationRepository.save(allocation);

        // Update booking status back to FORWARDED_TO_CREW
        Booking booking = allocation.getBooking();
        booking.setStatus(BookingStatus.FORWARDED_TO_CREW);
        booking.setUpdatedBy(currentUsername);
        bookingRepository.save(booking);

        log.info("Allocation cancelled: {} by {}", allocation.getId(), currentUsername);
    }

    public List<AllocationResponse> getAllAllocations() {
        return allocationRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<AllocationResponse> getActiveAllocations() {
        return allocationRepository.findByStatus(AllocationStatus.ACTIVE).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<AllocationResponse> getAllocationsByDriver(Long driverId) {
        return allocationRepository.findByDriverId(driverId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<AllocationResponse> getAllocationsByGuide(Long guideId) {
        return allocationRepository.findByGuideId(guideId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<AllocationResponse> getAllocationsByJeep(Long jeepId) {
        return allocationRepository.findByJeepId(jeepId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public AllocationResponse getAllocationById(Long allocationId) {
        Allocation allocation = allocationRepository.findById(allocationId)
            .orElseThrow(() -> new RuntimeException("Allocation not found"));
        return mapToResponse(allocation);
    }

    private AllocationResponse mapToResponse(Allocation allocation) {
        return AllocationResponse.builder()
            .id(allocation.getId())
            .bookingId(allocation.getBooking().getId())
            .touristName(allocation.getBooking().getTourist().getFullName())
            .packageName(allocation.getBooking().getTourPackage().getName())
            .requestedDate(allocation.getBooking().getRequestedDate())
            .requestedTime(allocation.getBooking().getRequestedTime())
            .driverId(allocation.getDriver() != null ? allocation.getDriver().getId() : null)
            .driverName(allocation.getDriver() != null ? allocation.getDriver().getFullName() : null)
            .driverPhone(allocation.getDriver() != null ? allocation.getDriver().getPhone() : null)
            .driverLicense(allocation.getDriver() != null ? allocation.getDriver().getLicenseNo() : null)
            .guideId(allocation.getGuide() != null ? allocation.getGuide().getId() : null)
            .guideName(allocation.getGuide() != null ? allocation.getGuide().getFullName() : null)
            .guidePhone(allocation.getGuide() != null ? allocation.getGuide().getPhone() : null)
            .jeepId(allocation.getJeep() != null ? allocation.getJeep().getId() : null)
            .jeepPlateNo(allocation.getJeep() != null ? allocation.getJeep().getPlateNo() : null)
            .jeepModel(allocation.getJeep() != null ? allocation.getJeep().getModel() : null)
            .jeepCapacity(allocation.getJeep() != null ? allocation.getJeep().getCapacity() : null)
            .status(allocation.getStatus())
            .notes(allocation.getNotes())
            .createdAt(allocation.getCreatedAt())
            .updatedAt(allocation.getUpdatedAt())
            .createdBy(allocation.getCreatedBy())
            .updatedBy(allocation.getUpdatedBy())
            .build();
    }
}
