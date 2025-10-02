package com.safari.safarims.repository;

import com.safari.safarims.entity.TourPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourPackageRepository extends JpaRepository<TourPackage, Long> {
    List<TourPackage> findByIsActiveTrue();
    List<TourPackage> findByNameContainingIgnoreCase(String name);
}
