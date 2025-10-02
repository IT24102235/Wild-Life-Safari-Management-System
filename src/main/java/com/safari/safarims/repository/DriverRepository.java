package com.safari.safarims.repository;

import com.safari.safarims.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUserEmail(String email);
    Optional<Driver> findByUserUsername(String username);
    Optional<Driver> findByLicenseNo(String licenseNo);
    boolean existsByLicenseNo(String licenseNo);
}
