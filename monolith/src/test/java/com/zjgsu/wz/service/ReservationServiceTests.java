package com.zjgsu.wz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.zjgsu.wz.dto.equipment.CreateEquipmentRequest;
import com.zjgsu.wz.dto.reservation.CreateReservationRequest;
import com.zjgsu.wz.exception.BusinessConflictException;
import com.zjgsu.wz.exception.InvalidRequestException;
import com.zjgsu.wz.model.Equipment;
import com.zjgsu.wz.model.Reservation;
import com.zjgsu.wz.model.ReservationStatus;

@SpringBootTest
@Transactional
class ReservationServiceTests {

    private Equipment equipment;
    @Autowired
    private EquipmentService equipmentService;
    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        equipment = equipmentService.create(new CreateEquipmentRequest(
                "CAM-001",
                "索尼微单相机",
                "摄影设备",
                new BigDecimal("120.00"),
                new BigDecimal("1000.00"),
                "课程测试设备"));
    }

    @Test
    void shouldCreateConfirmedReservation() {
        Reservation reservation = reservationService.create(request(
                "张三",
                LocalDateTime.of(2026, 10, 20, 9, 0),
                LocalDateTime.of(2026, 10, 21, 9, 0)));

        assertThat(reservation.getId()).isPositive();
        assertThat(reservation.getEquipment().getId()).isEqualTo(equipment.getId());
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void shouldRejectOverlappingReservation() {
        reservationService.create(request(
                "张三",
                LocalDateTime.of(2026, 10, 20, 9, 0),
                LocalDateTime.of(2026, 10, 21, 9, 0)));

        assertThatThrownBy(() -> reservationService.create(request(
                "李四",
                LocalDateTime.of(2026, 10, 20, 12, 0),
                LocalDateTime.of(2026, 10, 21, 12, 0))))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage("该设备在所选时间段已被预约");
    }

    @Test
    void shouldAllowAdjacentReservation() {
        LocalDateTime boundary = LocalDateTime.of(2026, 10, 21, 9, 0);
        reservationService.create(request(
                "张三",
                LocalDateTime.of(2026, 10, 20, 9, 0),
                boundary));

        Reservation adjacent = reservationService.create(request(
                "李四",
                boundary,
                LocalDateTime.of(2026, 10, 22, 9, 0)));

        assertThat(adjacent.getId()).isPositive();
    }

    @Test
    void shouldRejectInvalidTimeRange() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 20, 9, 0);

        assertThatThrownBy(() -> reservationService.create(request("张三", start, start)))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("预约结束时间必须晚于开始时间");
    }

    private CreateReservationRequest request(
            String renterName, LocalDateTime startTime, LocalDateTime endTime) {
        return new CreateReservationRequest(
                equipment.getId(), renterName, startTime, endTime);
    }
}
