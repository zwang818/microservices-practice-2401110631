package com.zjgsu.wz.dto.reservation;

import java.time.LocalDateTime;

import com.zjgsu.wz.model.Reservation;
import com.zjgsu.wz.model.ReservationStatus;

public record ReservationResponse(
        Long id,
        Long equipmentId,
        String equipmentName,
        String renterName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ReservationStatus status,
        LocalDateTime createdAt) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getEquipment().getId(),
                reservation.getEquipment().getName(),
                reservation.getRenterName(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                reservation.getCreatedAt());
    }
}
