package com.zjgsu.wz.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.zjgsu.wz.dto.reservation.CreateReservationRequest;
import com.zjgsu.wz.exception.BusinessConflictException;
import com.zjgsu.wz.exception.InvalidRequestException;
import com.zjgsu.wz.exception.ResourceNotFoundException;
import com.zjgsu.wz.model.Equipment;
import com.zjgsu.wz.model.EquipmentStatus;
import com.zjgsu.wz.model.Reservation;
import com.zjgsu.wz.model.ReservationStatus;

@Service
public class ReservationService {

    private final EquipmentService equipmentService;
    private final Map<Long, Reservation> reservationStore = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public ReservationService(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    public synchronized Reservation create(CreateReservationRequest request) {
        Equipment equipment = equipmentService.findById(request.equipmentId());
        validateTimeRange(request.startTime(), request.endTime());

        if (equipment.getStatus() != EquipmentStatus.AVAILABLE) {
            throw new BusinessConflictException("当前设备不可预约，状态: " + equipment.getStatus());
        }

        boolean overlaps = reservationStore.values().stream()
                .filter(reservation -> reservation.getEquipment().getId().equals(equipment.getId()))
                .filter(reservation -> reservation.getStatus() == ReservationStatus.CONFIRMED)
                .anyMatch(reservation -> isOverlapping(
                        reservation.getStartTime(),
                        reservation.getEndTime(),
                        request.startTime(),
                        request.endTime()));
        if (overlaps) {
            throw new BusinessConflictException("该设备在所选时间段已被预约");
        }

        Reservation reservation = new Reservation(
                idSequence.getAndIncrement(),
                equipment,
                request.renterName().trim(),
                request.startTime(),
                request.endTime(),
                ReservationStatus.CONFIRMED,
                LocalDateTime.now());
        reservationStore.put(reservation.getId(), reservation);
        return reservation;
    }

    public List<Reservation> findAll() {
        return reservationStore.values().stream()
                .sorted(Comparator.comparing(Reservation::getId))
                .toList();
    }

    public Reservation findById(Long id) {
        Reservation reservation = reservationStore.get(id);
        if (reservation == null) {
            throw new ResourceNotFoundException("预约不存在，ID: " + id);
        }
        return reservation;
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new InvalidRequestException("预约结束时间必须晚于开始时间");
        }
    }

    private boolean isOverlapping(
            LocalDateTime existingStart,
            LocalDateTime existingEnd,
            LocalDateTime requestedStart,
            LocalDateTime requestedEnd) {
        return existingStart.isBefore(requestedEnd)
                && existingEnd.isAfter(requestedStart);
    }
}
