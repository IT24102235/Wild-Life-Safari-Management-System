package com.safari.safarims.repository;

import com.safari.safarims.entity.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TouristRepository extends JpaRepository<Tourist, Long> {
    Optional<Tourist> findByUserEmail(String email);
    Optional<Tourist> findByUserUsername(String username);

    @Query("SELECT t FROM Tourist t JOIN FETCH t.preferredLanguages WHERE t.id = :id")
    Optional<Tourist> findByIdWithLanguages(@Param("id") Long id);
}
