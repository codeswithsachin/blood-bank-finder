package com.sachin.bloodbank.controller;

import com.sachin.bloodbank.model.*;
import com.sachin.bloodbank.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/donors")
public class DonorController {
    private final DonorService service;

    public DonorController(DonorService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Donor> register(@Valid @RequestBody Donor donor) {
        return ResponseEntity.ok(service.register(donor));
    }

    @GetMapping("/search")
    public List<Donor> search(@RequestParam BloodGroup bloodGroup,
                              @RequestParam String city) {
        return service.search(bloodGroup, city);
    }

    @PatchMapping("/{id}/donation-date")
    public Donor updateDonation(@PathVariable Long id, @RequestParam LocalDate date,
                                 @RequestParam(required = false) String location) {
        return service.updateDonation(id, date, location);
    }
}
