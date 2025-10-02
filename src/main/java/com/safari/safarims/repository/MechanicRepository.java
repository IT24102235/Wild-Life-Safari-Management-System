package com.safari.safarims.repository;

import com.safari.safarims.entity.Mechanic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MechanicRepository extends JpaRepository<Mechanic, Long> {
    Optional<Mechanic> findByUserEmail(String email);
    Optional<Mechanic> findByUserUsername(String username);
}
