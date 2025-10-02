package com.safari.safarims.repository;

import com.safari.safarims.entity.Jeep;
import com.safari.safarims.common.enums.JeepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JeepRepository extends JpaRepository<Jeep, Long> {
    List<Jeep> findByStatus(JeepStatus status);
    List<Jeep> findByStatusIn(List<JeepStatus> statuses);
    Optional<Jeep> findByPlateNo(String plateNo);
    List<Jeep> findByDefaultDriverId(Long driverId);
}
