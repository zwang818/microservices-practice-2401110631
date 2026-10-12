package com.zjgsu.wz.dto.equipment;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateEquipmentRequest(
        @NotBlank(message = "设备编号不能为空") String code,
        @NotBlank(message = "设备名称不能为空") String name,
        @NotBlank(message = "设备分类不能为空") String category,
        @NotNull(message = "日租金不能为空")
        @PositiveOrZero(message = "日租金不能为负数") BigDecimal dailyRate,
        @NotNull(message = "押金不能为空")
        @PositiveOrZero(message = "押金不能为负数") BigDecimal deposit,
        String description) {
}
