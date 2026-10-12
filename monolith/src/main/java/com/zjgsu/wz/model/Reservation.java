package com.zjgsu.wz.model;

import java.time.LocalDateTime;

public class Reservation {

    private final Long id;
    private final Equipment equipment;
    private final String renterName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final ReservationStatus status;
    private final LocalDateTime createdAt;

    public Reservation(Long id, Equipment equipment, String renterName,
            LocalDateTime startTime, LocalDateTime endTime,
            ReservationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.equipment = equipment;
        this.renterName = renterName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public String getRenterName() {
        return renterName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
