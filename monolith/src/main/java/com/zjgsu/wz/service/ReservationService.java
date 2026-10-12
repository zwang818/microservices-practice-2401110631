package com.zjgsu.wz.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zjgsu.wz.dto.reservation.CreateReservationRequest;
import com.zjgsu.wz.exception.BusinessConflictException;
import com.zjgsu.wz.exception.InvalidRequestException;
import com.zjgsu.wz.exception.ResourceNotFoundException;
import com.zjgsu.wz.model.Equipment;
import com.zjgsu.wz.model.EquipmentStatus;
import com.zjgsu.wz.model.Reservation;
import com.zjgsu.wz.model.ReservationStatus;
import com.zjgsu.wz.repository.EquipmentRepository;
import com.zjgsu.wz.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final EquipmentRepository equipmentRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(EquipmentRepository equipmentRepository, ReservationRepository reservationRepository) {
        this.equipmentRepository = equipmentRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation create(CreateReservationRequest request) {
        validateTimeRange(request.startTime(), request.endTime());
        // 锁定设备直到事务提交，使同一设备的冲突检查和保存顺序执行。
        Equipment equipment = equipmentRepository.findByIdForUpdate(request.equipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("设备不存在，ID: " + request.equipmentId()));

        if (equipment.getStatus() != EquipmentStatus.AVAILABLE) {
            throw new BusinessConflictException("当前设备不可预约，状态: " + equipment.getStatus());
        }

        boolean overlaps = reservationRepository.existsByEquipmentIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                equipment.getId(), ReservationStatus.CONFIRMED, request.endTime(), request.startTime());
        if (overlaps) {
            throw new BusinessConflictException("该设备在所选时间段已被预约");
        }

        Reservation reservation = new Reservation(
                equipment,
                request.renterName().trim(),
                request.startTime(),
                request.endTime(),
                ReservationStatus.CONFIRMED,
                LocalDateTime.now());
        return reservationRepository.saveAndFlush(reservation);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAllByOrderByIdAsc();
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("预约不存在，ID: " + id));
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new InvalidRequestException("预约结束时间必须晚于开始时间");
        }
    }

}
