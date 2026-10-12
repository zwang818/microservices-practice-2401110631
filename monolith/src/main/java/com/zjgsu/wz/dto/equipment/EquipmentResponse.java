package com.zjgsu.wz.dto.equipment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.zjgsu.wz.model.Equipment;
import com.zjgsu.wz.model.EquipmentStatus;

public record EquipmentResponse(
        Long id,
        String code,
        String name,
        String category,
        BigDecimal dailyRate,
        BigDecimal deposit,
        EquipmentStatus status,
        String description,
        LocalDateTime createdAt) {

    public static EquipmentResponse from(Equipment equipment) {
        return new EquipmentResponse(
                equipment.getId(),
                equipment.getCode(),
                equipment.getName(),
                equipment.getCategory(),
                equipment.getDailyRate(),
                equipment.getDeposit(),
                equipment.getStatus(),
                equipment.getDescription(),
                equipment.getCreatedAt());
    }
}
