package com.sachin.bloodbank.controller;

import com.sachin.bloodbank.model.RequestStatus;
import com.sachin.bloodbank.repository.BloodRequestRepository;
import com.sachin.bloodbank.repository.DonorRepository;
import com.sachin.bloodbank.repository.HospitalRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Landing page. Stats shown here are live counts from the database, not
 * placeholder marketing numbers — deliberately, so the page never claims
 * usage figures the app hasn't actually earned yet.
 */
@Controller
public class HomeController {
    private final DonorRepository donors;
    private final HospitalRepository hospitals;
    private final BloodRequestRepository requests;

    public HomeController(DonorRepository donors, HospitalRepository hospitals, BloodRequestRepository requests) {
        this.donors = donors;
        this.hospitals = hospitals;
        this.requests = requests;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("donorCount", donors.count());
        model.addAttribute("hospitalCount", hospitals.count());
        model.addAttribute("openRequestCount", requests.findByStatusOrderByCreatedAtDesc(RequestStatus.OPEN).size());
        return "index";
    }
}
