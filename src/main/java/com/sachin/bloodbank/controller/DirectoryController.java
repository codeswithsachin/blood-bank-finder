package com.sachin.bloodbank.controller;

import com.sachin.bloodbank.dto.MapMarker;
import com.sachin.bloodbank.model.BloodBank;
import com.sachin.bloodbank.model.BloodGroup;
import com.sachin.bloodbank.repository.BloodBankRepository;
import com.sachin.bloodbank.repository.BloodCampRepository;
import com.sachin.bloodbank.repository.HospitalRepository;
import com.sachin.bloodbank.service.DonorMatchingService;
import com.sachin.bloodbank.util.CityCoordinates;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DirectoryController {
    private final HospitalRepository hospitals;
    private final BloodBankRepository bloodBanks;
    private final BloodCampRepository camps;
    private final DonorMatchingService matchingService;

    public DirectoryController(HospitalRepository hospitals, BloodBankRepository bloodBanks,
                                BloodCampRepository camps, DonorMatchingService matchingService) {
        this.hospitals = hospitals;
        this.bloodBanks = bloodBanks;
        this.camps = camps;
        this.matchingService = matchingService;
    }

    private List<MapMarker> hospitalMarkers() {
        return hospitals.findAll().stream()
                .map(h -> new MapMarker(h.getName(),
                        (h.getArea() != null ? h.getArea() + ", " : "") + h.getCity()
                                + (h.isHasBloodBank() ? " (has a blood bank)" : ""),
                        h.getLatitude(), h.getLongitude(), "hospital"))
                .toList();
    }

    private List<MapMarker> bloodBankMarkers() {
        List<MapMarker> markers = new ArrayList<>();
        for (BloodBank bank : bloodBanks.findAll()) {
            double[] coords = CityCoordinates.lookup(bank.getCity());
            if (coords == null) continue;
            String subtitle = bank.getBloodGroup().getSymbol() + " · " + bank.getUnitsAvailable() + " units available";
            markers.add(new MapMarker(bank.getName(), subtitle, coords[0], coords[1], "bloodbank"));
        }
        return markers;
    }

    @GetMapping("/api/map-data")
    @ResponseBody
    public List<MapMarker> mapData() {
        List<MapMarker> markers = new ArrayList<>(hospitalMarkers());
        markers.addAll(bloodBankMarkers());
        return markers;
    }

    @GetMapping("/hospitals")
    public String hospitalsPage(Model model) {
        model.addAttribute("hospitals", hospitals.findAll());
        return "hospitals";
    }

    @GetMapping("/find-donors")
    public String findDonors(@RequestParam(required = false) BloodGroup bloodGroup,
                              @RequestParam(required = false) String city, Model model) {
        model.addAttribute("bloodGroups", BloodGroup.values());

        if (bloodGroup != null && city != null && !city.isBlank()) {
            List<DonorMatchingService.CompatibilityMatch> matches = matchingService.findCompatibleSummary(bloodGroup, city);
            model.addAttribute("matches", matches);
            model.addAttribute("searchedCity", city);
            model.addAttribute("searchedGroup", bloodGroup);

            double[] coords = CityCoordinates.lookup(city);
            if (coords != null) {
                long total = matches.stream().mapToLong(DonorMatchingService.CompatibilityMatch::eligibleCount).sum();
                model.addAttribute("searchMarker", new MapMarker("Eligible donors near you",
                        total + " potential donor(s) found in " + city, coords[0], coords[1], "search"));
            }
        }

        return "find-donors";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/camps")
    public String camps(Model model) {
        model.addAttribute("camps", camps.findByCampDateGreaterThanEqualOrderByCampDateAsc(LocalDate.now()));
        return "camps";
    }
}
