package com.safari.safarims.repository;

import com.safari.safarims.entity.Allocation;
import com.safari.safarims.common.enums.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    Optional<Allocation> findByBookingId(Long bookingId);
    List<Allocation> findByDriverId(Long driverId);
    List<Allocation> findByGuideId(Long guideId);
    List<Allocation> findByJeepId(Long jeepId);
    List<Allocation> findByStatus(AllocationStatus status);

    @Query("SELECT a FROM Allocation a WHERE a.status = 'ACTIVE' AND a.jeep.id = :jeepId")
    List<Allocation> findActiveAllocationsByJeepId(@Param("jeepId") Long jeepId);

    @Query("SELECT a FROM Allocation a WHERE a.status = 'ACTIVE' AND a.driver.id = :driverId")
    List<Allocation> findActiveAllocationsByDriverId(@Param("driverId") Long driverId);

    @Query("SELECT a FROM Allocation a WHERE a.status = 'ACTIVE' AND a.guide.id = :guideId")
    List<Allocation> findActiveAllocationsByGuideId(@Param("guideId") Long guideId);
}
