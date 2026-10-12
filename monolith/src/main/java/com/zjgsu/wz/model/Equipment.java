package com.zjgsu.wz.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Equipment {

    private final Long id;
    private final String code;
    private final String name;
    private final String category;
    private final BigDecimal dailyRate;
    private final BigDecimal deposit;
    private final EquipmentStatus status;
    private final String description;
    private final LocalDateTime createdAt;

    public Equipment(Long id, String code, String name, String category,
            BigDecimal dailyRate, BigDecimal deposit, EquipmentStatus status,
            String description, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
        this.dailyRate = dailyRate;
        this.deposit = deposit;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
