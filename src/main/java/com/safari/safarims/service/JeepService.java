package com.safari.safarims.service;

import com.safari.safarims.dto.jeep.JeepRequest;
import com.safari.safarims.dto.jeep.JeepResponse;
import com.safari.safarims.entity.Jeep;
import com.safari.safarims.entity.Driver;
import com.safari.safarims.common.enums.VehicleStatus;
import com.safari.safarims.common.enums.AllocationStatus;
import com.safari.safarims.repository.JeepRepository;
import com.safari.safarims.repository.DriverRepository;
import com.safari.safarims.repository.AllocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JeepService {

    private final JeepRepository jeepRepository;
    private final DriverRepository driverRepository;
    private final AllocationRepository allocationRepository;

    public List<JeepResponse> getAllJeeps() {
        return jeepRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<JeepResponse> getAvailableJeeps() {
        List<VehicleStatus> availableStatuses = List.of(VehicleStatus.AVAILABLE);
        return jeepRepository.findByStatusIn(availableStatuses).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public JeepResponse getJeepById(Long id) {
        Jeep jeep = jeepRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Jeep not found with id: " + id));
        return mapToResponse(jeep);
    }

    @Transactional
    public JeepResponse createJeep(JeepRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Check if plate number already exists
        if (jeepRepository.findByPlateNo(request.getPlateNo()).isPresent()) {
            throw new RuntimeException("Jeep with plate number " + request.getPlateNo() + " already exists");
        }

        // Validate default driver if provided
        Driver defaultDriver = null;
        if (request.getDefaultDriverId() != null) {
            defaultDriver = driverRepository.findById(request.getDefaultDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        }

        Jeep jeep = Jeep.builder()
            .plateNo(request.getPlateNo())
            .model(request.getModel())
            .capacity(request.getCapacity())
            .defaultDriver(defaultDriver)
            .status(VehicleStatus.AVAILABLE)
            .build();

        jeep.setCreatedBy(currentUsername);
        jeep.setUpdatedBy(currentUsername);

        Jeep saved = jeepRepository.save(jeep);
        log.info("Jeep created: {} by {}", saved.getPlateNo(), currentUsername);

        return mapToResponse(saved);
    }

    @Transactional
    public JeepResponse updateJeep(Long id, JeepRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Jeep jeep = jeepRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Jeep not found with id: " + id));

        // Check if plate number is being changed to an existing one
        if (!jeep.getPlateNo().equals(request.getPlateNo())) {
            if (jeepRepository.findByPlateNo(request.getPlateNo()).isPresent()) {
                throw new RuntimeException("Jeep with plate number " + request.getPlateNo() + " already exists");
            }
        }

        // Validate default driver if provided
        Driver defaultDriver = null;
        if (request.getDefaultDriverId() != null) {
            defaultDriver = driverRepository.findById(request.getDefaultDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        }

        jeep.setPlateNo(request.getPlateNo());
        jeep.setModel(request.getModel());
        jeep.setCapacity(request.getCapacity());
        jeep.setDefaultDriver(defaultDriver);
        jeep.setUpdatedBy(currentUsername);

        Jeep updated = jeepRepository.save(jeep);
        log.info("Jeep updated: {} by {}", updated.getPlateNo(), currentUsername);

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteJeep(Long id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Jeep jeep = jeepRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Jeep not found with id: " + id));

        // Check if jeep has active allocations
        List<com.safari.safarims.entity.Allocation> activeAllocations =
            allocationRepository.findActiveAllocationsByJeepId(id);

        if (!activeAllocations.isEmpty()) {
            throw new RuntimeException("Cannot delete jeep with active allocations");
        }

        jeepRepository.delete(jeep);
        log.info("Jeep deleted: {} by {}", jeep.getPlateNo(), currentUsername);
    }

    @Transactional
    public void setDefaultDriver(Long jeepId, Long driverId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Jeep jeep = jeepRepository.findById(jeepId)
            .orElseThrow(() -> new RuntimeException("Jeep not found"));

        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new RuntimeException("Driver not found"));

        jeep.setDefaultDriver(driver);
        jeep.setUpdatedBy(currentUsername);
        jeepRepository.save(jeep);

        log.info("Default driver set for jeep {}: {} by {}",
            jeep.getPlateNo(), driver.getFullName(), currentUsername);
    }

    @Transactional
    public void updateJeepStatus(Long jeepId, VehicleStatus status) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Jeep jeep = jeepRepository.findById(jeepId)
            .orElseThrow(() -> new RuntimeException("Jeep not found"));

        jeep.setStatus(status);
        jeep.setUpdatedBy(currentUsername);
        jeepRepository.save(jeep);

        log.info("Jeep {} status updated to {} by {}",
            jeep.getPlateNo(), status, currentUsername);
    }

    public List<JeepResponse> getJeepsByDriver(Long driverId) {
        return jeepRepository.findByDefaultDriverId(driverId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private JeepResponse mapToResponse(Jeep jeep) {
        int activeAllocationsCount = allocationRepository.findActiveAllocationsByJeepId(jeep.getId()).size();

        return JeepResponse.builder()
            .id(jeep.getId())
            .plateNo(jeep.getPlateNo())
            .model(jeep.getModel())
            .capacity(jeep.getCapacity())
            .status(jeep.getStatus())
            .defaultDriverId(jeep.getDefaultDriver() != null ? jeep.getDefaultDriver().getId() : null)
            .defaultDriverName(jeep.getDefaultDriver() != null ? jeep.getDefaultDriver().getFullName() : null)
            .defaultDriverPhone(jeep.getDefaultDriver() != null ? jeep.getDefaultDriver().getPhone() : null)
            .defaultDriverLicense(jeep.getDefaultDriver() != null ? jeep.getDefaultDriver().getLicenseNo() : null)
            .createdAt(jeep.getCreatedAt())
            .updatedAt(jeep.getUpdatedAt())
            .createdBy(jeep.getCreatedBy())
            .updatedBy(jeep.getUpdatedBy())
            .isAvailable(jeep.getStatus() == VehicleStatus.AVAILABLE)
            .activeAllocationsCount(activeAllocationsCount)
            .build();
    }
}
