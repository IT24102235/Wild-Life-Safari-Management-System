package com.safari.safarims.repository;

import com.safari.safarims.entity.Booking;
import com.safari.safarims.common.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByTouristId(Long touristId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByRequestedDate(LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.status IN :statuses ORDER BY b.createdAt DESC")
    List<Booking> findByStatusIn(@Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT b FROM Booking b WHERE b.tourist.id = :touristId ORDER BY b.createdAt DESC")
    List<Booking> findByTouristIdOrderByCreatedAtDesc(@Param("touristId") Long touristId);
}
