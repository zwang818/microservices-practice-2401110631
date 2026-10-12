package com.zjgsu.wz.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.zjgsu.wz.dto.equipment.CreateEquipmentRequest;
import com.zjgsu.wz.service.EquipmentService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PersistenceApiTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private EquipmentService equipmentService;

    @Test
    void shouldCreateAndQueryEquipmentThroughApi() throws Exception {
        mvc.perform(post("/api/equipment").contentType(MediaType.APPLICATION_JSON).content("""
                {"code":"API-CAM-001","name":"接口测试相机","category":"摄影设备","dailyRate":120,"deposit":1000}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
        mvc.perform(get("/api/equipment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("API-CAM-001"));
    }

    @Test
    void shouldCreateReadAndRejectConflictingReservation() throws Exception {
        Long id = equipmentService.create(new CreateEquipmentRequest("API-CAM-002", "接口测试相机", "摄影设备",
                new BigDecimal("120.00"), new BigDecimal("1000.00"), null)).getId();
        String request = """
                {"equipmentId":%d,"renterName":"接口测试用户","startTime":"2026-10-20T09:00:00","endTime":"2026-10-21T09:00:00"}
                """.formatted(id);
        mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        mvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].equipmentName").value("接口测试相机"));
        mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("该设备在所选时间段已被预约"));
    }

    @Test
    void shouldReturnBadRequestAndNotFound() throws Exception {
        mvc.perform(post("/api/equipment").contentType(MediaType.APPLICATION_JSON).content("""
                {"code":"API-CAM-INVALID","name":"错误请求","category":"摄影设备","dailyRate":-1,"deposit":1000}
                """))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/reservations/999999"))
                .andExpect(status().isNotFound());
    }
}
