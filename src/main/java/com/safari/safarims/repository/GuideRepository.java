package com.safari.safarims.repository;

import com.safari.safarims.entity.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {
    Optional<Guide> findByUserEmail(String email);
    Optional<Guide> findByUserUsername(String username);

    @Query("SELECT g FROM Guide g JOIN FETCH g.languages WHERE g.id = :id")
    Optional<Guide> findByIdWithLanguages(@Param("id") Long id);

    @Query("SELECT g FROM Guide g JOIN g.languages l WHERE l.isoCode IN :languageCodes")
    List<Guide> findByLanguageCodes(@Param("languageCodes") List<String> languageCodes);
}
