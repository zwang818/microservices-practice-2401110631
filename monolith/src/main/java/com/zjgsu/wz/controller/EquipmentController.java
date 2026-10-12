package com.zjgsu.wz.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zjgsu.wz.dto.equipment.CreateEquipmentRequest;
import com.zjgsu.wz.dto.equipment.EquipmentResponse;
import com.zjgsu.wz.service.EquipmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentResponse create(@Valid @RequestBody CreateEquipmentRequest request) {
        return EquipmentResponse.from(equipmentService.create(request));
    }

    @GetMapping
    public List<EquipmentResponse> findAll() {
        return equipmentService.findAll().stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public EquipmentResponse findById(@PathVariable Long id) {
        return EquipmentResponse.from(equipmentService.findById(id));
    }
}
