package com.zjgsu.wz.dto.reservation;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateReservationRequest(
        @NotNull(message = "设备 ID 不能为空")
        @Positive(message = "设备 ID 必须为正数") Long equipmentId,
        @NotBlank(message = "租用人姓名不能为空") @Size(max = 100) String renterName,
        @NotNull(message = "预约开始时间不能为空") LocalDateTime startTime,
        @NotNull(message = "预约结束时间不能为空") LocalDateTime endTime) {
}
