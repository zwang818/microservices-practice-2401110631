package com.zjgsu.wz.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "equipment", uniqueConstraints = @UniqueConstraint(name = "uk_equipment_code", columnNames = "code"))
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 64)
    private String code;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 50)
    private String category;
    @Column(name = "daily_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyRate;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deposit;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private EquipmentStatus status;
    @Column(length = 1000)
    private String description;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Equipment() {
    }

    public Equipment(String code, String name, String category,
            BigDecimal dailyRate, BigDecimal deposit, EquipmentStatus status,
            String description, LocalDateTime createdAt) {
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
