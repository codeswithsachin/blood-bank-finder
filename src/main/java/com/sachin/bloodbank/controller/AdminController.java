package com.sachin.bloodbank.controller;

import com.sachin.bloodbank.model.BloodBank;
import com.sachin.bloodbank.model.BloodGroup;
import com.sachin.bloodbank.repository.BloodBankRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/blood-banks")
public class AdminController {
    private final BloodBankRepository repository;

    public AdminController(BloodBankRepository repository) { this.repository = repository; }

    @PostMapping
    public BloodBank create(@RequestBody BloodBank bank) { return repository.save(bank); }

    @GetMapping
    public List<BloodBank> list(@RequestParam BloodGroup bloodGroup,
                                @RequestParam String city) {
        return repository.findByBloodGroupAndCityIgnoreCase(bloodGroup, city);
    }

    @PatchMapping("/{id}/stock")
    public BloodBank updateStock(@PathVariable Long id, @RequestParam int units) {
        BloodBank bank = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blood bank not found"));
        bank.setUnitsAvailable(units);
        return repository.save(bank);
    }
}
