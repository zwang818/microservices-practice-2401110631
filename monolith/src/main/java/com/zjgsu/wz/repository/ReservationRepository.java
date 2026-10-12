package com.zjgsu.wz.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.zjgsu.wz.model.Reservation;
import com.zjgsu.wz.model.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByEquipmentIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Long equipmentId, ReservationStatus status, LocalDateTime requestedEnd, LocalDateTime requestedStart);

    @EntityGraph(attributePaths = "equipment")
    List<Reservation> findAllByOrderByIdAsc();

    @Override
    @EntityGraph(attributePaths = "equipment")
    Optional<Reservation> findById(Long id);
}
