package com.zjgsu.wz.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.zjgsu.wz.dto.equipment.CreateEquipmentRequest;
import com.zjgsu.wz.exception.BusinessConflictException;
import com.zjgsu.wz.exception.ResourceNotFoundException;
import com.zjgsu.wz.model.Equipment;
import com.zjgsu.wz.model.EquipmentStatus;

@Service
public class EquipmentService {

    private final Map<Long, Equipment> equipmentStore = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public synchronized Equipment create(CreateEquipmentRequest request) {
        String normalizedCode = request.code().trim();
        boolean codeExists = equipmentStore.values().stream()
                .anyMatch(equipment -> equipment.getCode().equalsIgnoreCase(normalizedCode));
        if (codeExists) {
            throw new BusinessConflictException("设备编号已存在: " + normalizedCode);
        }

        Equipment equipment = new Equipment(
                idSequence.getAndIncrement(),
                normalizedCode,
                request.name().trim(),
                request.category().trim(),
                request.dailyRate(),
                request.deposit(),
                EquipmentStatus.AVAILABLE,
                normalizeNullable(request.description()),
                LocalDateTime.now());
        equipmentStore.put(equipment.getId(), equipment);
        return equipment;
    }

    public List<Equipment> findAll() {
        return equipmentStore.values().stream()
                .sorted(Comparator.comparing(Equipment::getId))
                .toList();
    }

    public Equipment findById(Long id) {
        Equipment equipment = equipmentStore.get(id);
        if (equipment == null) {
            throw new ResourceNotFoundException("设备不存在，ID: " + id);
        }
        return equipment;
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
