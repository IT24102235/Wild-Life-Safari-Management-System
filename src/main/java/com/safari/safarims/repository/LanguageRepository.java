package com.safari.safarims.repository;

import com.safari.safarims.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
    List<Language> findByIsActiveTrue();
    Optional<Language> findByIsoCode(String isoCode);
    List<Language> findByNameContainingIgnoreCase(String name);
}
