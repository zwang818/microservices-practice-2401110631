package com.zjgsu.wz.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zjgsu.wz.dto.equipment.CreateEquipmentRequest;
import com.zjgsu.wz.exception.BusinessConflictException;
import com.zjgsu.wz.exception.ResourceNotFoundException;
import com.zjgsu.wz.model.Equipment;
import com.zjgsu.wz.model.EquipmentStatus;
import com.zjgsu.wz.repository.EquipmentRepository;

@Service
@Transactional(readOnly = true)
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional
    public Equipment create(CreateEquipmentRequest request) {
        String normalizedCode = request.code().trim().toUpperCase(Locale.ROOT);
        if (equipmentRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new BusinessConflictException("设备编号已存在: " + normalizedCode);
        }

        Equipment equipment = new Equipment(
                normalizedCode,
                request.name().trim(),
                request.category().trim(),
                request.dailyRate(),
                request.deposit(),
                EquipmentStatus.AVAILABLE,
                normalizeNullable(request.description()),
                LocalDateTime.now());
        try {
            return equipmentRepository.saveAndFlush(equipment);
        } catch (DataIntegrityViolationException exception) {
            // 数据库唯一约束仍可拦截两个并发请求创建相同编号。
            throw new BusinessConflictException("设备编号已存在: " + normalizedCode);
        }
    }

    public List<Equipment> findAll() {
        return equipmentRepository.findAllByOrderByIdAsc();
    }

    public Equipment findById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("设备不存在，ID: " + id));
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
