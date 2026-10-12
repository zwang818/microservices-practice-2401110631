package com.zjgsu.wz.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.zjgsu.wz.model.Equipment;
import com.zjgsu.wz.model.EquipmentStatus;
import com.zjgsu.wz.model.Reservation;
import com.zjgsu.wz.model.ReservationStatus;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class RepositoryTests {

    @Autowired
    private EquipmentRepository equipmentRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReadEquipmentAndReservationBackFromDatabase() {
        Equipment equipment = equipmentRepository.saveAndFlush(equipment("REPO-CAM-001"));
        LocalDateTime start = LocalDateTime.of(2026, 10, 20, 9, 0);
        Reservation reservation = reservationRepository.saveAndFlush(new Reservation(
                equipment, "测试用户", start, start.plusDays(1), ReservationStatus.CONFIRMED, LocalDateTime.now()));
        Long equipmentId = equipment.getId();
        Long reservationId = reservation.getId();
        entityManager.clear();

        Equipment storedEquipment = equipmentRepository.findById(equipmentId).orElseThrow();
        Reservation storedReservation = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(storedEquipment.getDailyRate()).isEqualByComparingTo("120.00");
        assertThat(storedReservation.getEquipment().getId()).isEqualTo(equipmentId);
        assertThat(storedReservation.getEquipment().getName()).isEqualTo("测试相机");
        assertThat(storedReservation.getRenterName()).isEqualTo("测试用户");
        assertThat(storedReservation.getStartTime()).isEqualTo(start);
    }

    @Test
    void shouldFindOnlyOverlappingConfirmedReservations() {
        Equipment equipment = equipmentRepository.saveAndFlush(equipment("REPO-CAM-002"));
        LocalDateTime start = LocalDateTime.of(2026, 10, 20, 9, 0);
        LocalDateTime end = start.plusDays(1);
        reservationRepository.saveAndFlush(new Reservation(
                equipment, "有效预约", start, end, ReservationStatus.CONFIRMED, LocalDateTime.now()));
        reservationRepository.saveAndFlush(new Reservation(
                equipment, "已取消预约", end, end.plusDays(1), ReservationStatus.CANCELLED, LocalDateTime.now()));

        assertThat(overlaps(equipment.getId(), start.plusHours(1), end.plusHours(1))).isTrue();
        assertThat(overlaps(equipment.getId(), end, end.plusDays(1))).isFalse();
    }

    @Test
    void shouldEnforceUniqueEquipmentCodeInDatabase() {
        equipmentRepository.saveAndFlush(equipment("REPO-CAM-003"));
        entityManager.clear();

        assertThatThrownBy(() -> equipmentRepository.saveAndFlush(equipment("REPO-CAM-003")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private boolean overlaps(Long equipmentId, LocalDateTime start, LocalDateTime end) {
        return reservationRepository.existsByEquipmentIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                equipmentId, ReservationStatus.CONFIRMED, end, start);
    }

    private Equipment equipment(String code) {
        return new Equipment(code, "测试相机", "摄影设备", new BigDecimal("120.00"),
                new BigDecimal("1000.00"), EquipmentStatus.AVAILABLE, "Repository 测试", LocalDateTime.now());
    }
}
