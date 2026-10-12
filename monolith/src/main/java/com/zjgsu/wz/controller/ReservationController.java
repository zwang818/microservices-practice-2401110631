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

import com.zjgsu.wz.dto.reservation.CreateReservationRequest;
import com.zjgsu.wz.dto.reservation.ReservationResponse;
import com.zjgsu.wz.service.ReservationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody CreateReservationRequest request) {
        return ReservationResponse.from(reservationService.create(request));
    }

    @GetMapping
    public List<ReservationResponse> findAll() {
        return reservationService.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ReservationResponse findById(@PathVariable Long id) {
        return ReservationResponse.from(reservationService.findById(id));
    }
}
