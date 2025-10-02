package com.safari.safarims.service;

import com.safari.safarims.dto.tourpackage.TourPackageRequest;
import com.safari.safarims.dto.tourpackage.TourPackageResponse;
import com.safari.safarims.entity.TourPackage;
import com.safari.safarims.repository.TourPackageRepository;
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
public class TourPackageService {

    private final TourPackageRepository tourPackageRepository;

    public List<TourPackageResponse> getAllActivePackages() {
        return tourPackageRepository.findByIsActiveTrue().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<TourPackageResponse> getAllPackages() {
        return tourPackageRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public TourPackageResponse getPackageById(Long id) {
        TourPackage tourPackage = tourPackageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tour package not found with id: " + id));
        return mapToResponse(tourPackage);
    }

    @Transactional
    public TourPackageResponse createPackage(TourPackageRequest request) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        TourPackage tourPackage = TourPackage.builder()
            .name(request.getName())
            .days(request.getDays())
            .maxPeople(request.getMaxPeople())
            .price(request.getPrice())
            .description(request.getDescription())
            .isActive(request.getIsActive())
            .build();

        tourPackage.setCreatedBy(currentUser);
        tourPackage.setUpdatedBy(currentUser);

        TourPackage saved = tourPackageRepository.save(tourPackage);
        log.info("Tour package created: {} by {}", saved.getName(), currentUser);

        return mapToResponse(saved);
    }

    @Transactional
    public TourPackageResponse updatePackage(Long id, TourPackageRequest request) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        TourPackage tourPackage = tourPackageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tour package not found with id: " + id));

        tourPackage.setName(request.getName());
        tourPackage.setDays(request.getDays());
        tourPackage.setMaxPeople(request.getMaxPeople());
        tourPackage.setPrice(request.getPrice());
        tourPackage.setDescription(request.getDescription());
        tourPackage.setIsActive(request.getIsActive());
        tourPackage.setUpdatedBy(currentUser);

        TourPackage updated = tourPackageRepository.save(tourPackage);
        log.info("Tour package updated: {} by {}", updated.getName(), currentUser);

        return mapToResponse(updated);
    }

    @Transactional
    public void deletePackage(Long id) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        TourPackage tourPackage = tourPackageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tour package not found with id: " + id));

        // Soft delete by setting isActive to false
        tourPackage.setIsActive(false);
        tourPackage.setUpdatedBy(currentUser);
        tourPackageRepository.save(tourPackage);

        log.info("Tour package deactivated: {} by {}", tourPackage.getName(), currentUser);
    }

    public List<TourPackageResponse> searchPackages(String name) {
        return tourPackageRepository.findByNameContainingIgnoreCase(name).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private TourPackageResponse mapToResponse(TourPackage tourPackage) {
        return TourPackageResponse.builder()
            .id(tourPackage.getId())
            .name(tourPackage.getName())
            .days(tourPackage.getDays())
            .maxPeople(tourPackage.getMaxPeople())
            .price(tourPackage.getPrice())
            .description(tourPackage.getDescription())
            .isActive(tourPackage.getIsActive())
            .createdAt(tourPackage.getCreatedAt())
            .updatedAt(tourPackage.getUpdatedAt())
            .createdBy(tourPackage.getCreatedBy())
            .updatedBy(tourPackage.getUpdatedBy())
            .build();
    }
}
