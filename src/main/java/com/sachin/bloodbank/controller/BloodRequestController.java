package com.sachin.bloodbank.controller;

import com.sachin.bloodbank.model.BloodRequest;
import com.sachin.bloodbank.service.BloodRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class BloodRequestController {
    private final BloodRequestService service;

    public BloodRequestController(BloodRequestService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<BloodRequest> create(@RequestBody BloodRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping("/open")
    public List<BloodRequest> open() { return service.openRequests(); }

    @PostMapping("/{id}/close")
    public BloodRequest close(@PathVariable Long id) { return service.close(id); }
}
