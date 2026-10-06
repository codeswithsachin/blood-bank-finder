package com.sachin.bloodbank.controller;

import com.sachin.bloodbank.model.*;
import com.sachin.bloodbank.repository.BloodBankRepository;
import com.sachin.bloodbank.repository.BloodRequestRepository;
import com.sachin.bloodbank.repository.DonorRepository;
import com.sachin.bloodbank.repository.HospitalRepository;
import com.sachin.bloodbank.service.AiInsightService;
import com.sachin.bloodbank.util.CityCoordinates;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    private final BloodBankRepository bloodBanks;
    private final BloodRequestRepository requests;
    private final DonorRepository donors;
    private final HospitalRepository hospitals;
    private final AiInsightService aiInsightService;

    public AdminDashboardController(BloodBankRepository bloodBanks, BloodRequestRepository requests,
                                     DonorRepository donors, HospitalRepository hospitals,
                                     AiInsightService aiInsightService) {
        this.bloodBanks = bloodBanks;
        this.requests = requests;
        this.donors = donors;
        this.hospitals = hospitals;
        this.aiInsightService = aiInsightService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String filterCity,
                             @RequestParam(required = false) BloodGroup filterGroup,
                             Model model) {
        model.addAttribute("bloodBanks", bloodBanks.findAll());
        model.addAttribute("openRequests", requests.findByStatusOrderByCreatedAtDesc(RequestStatus.OPEN));

        List<Donor> filteredDonors = donors.findAll().stream()
                .filter(d -> filterCity == null || filterCity.isBlank() || d.getCity().equalsIgnoreCase(filterCity.trim()))
                .filter(d -> filterGroup == null || d.getBloodGroup() == filterGroup)
                .toList();
        model.addAttribute("donors", filteredDonors);
        model.addAttribute("donorCount", donors.count());
        model.addAttribute("filterCity", filterCity);
        model.addAttribute("filterGroup", filterGroup);
        model.addAttribute("bloodGroups", BloodGroup.values());
        model.addAttribute("hospitals", hospitals.findAll());
        model.addAttribute("insights", aiInsightService.generate());

        // Chart data: total units per blood group, across all listed blood banks.
        // Keyed by the clean display symbol ("O+"), not the raw enum name, so the
        // chart labels never show underscores when serialized to JSON for Chart.js.
        Map<String, Integer> stockByGroup = new LinkedHashMap<>();
        for (BloodGroup g : BloodGroup.values()) stockByGroup.put(g.getSymbol(), 0);
        for (BloodBank bank : bloodBanks.findAll()) {
            stockByGroup.merge(bank.getBloodGroup().getSymbol(), bank.getUnitsAvailable(), Integer::sum);
        }
        model.addAttribute("stockByGroup", stockByGroup);
        model.addAttribute("openCount", requests.findByStatusOrderByCreatedAtDesc(RequestStatus.OPEN).size());
        model.addAttribute("closedCount", requests.findByStatusOrderByCreatedAtDesc(RequestStatus.CLOSED).size());

        return "admin-dashboard";
    }

    @PostMapping("/blood-banks")
    public String addBloodBank(@RequestParam String name, @RequestParam String city,
                                @RequestParam String address, @RequestParam String phone,
                                @RequestParam BloodGroup bloodGroup, @RequestParam int unitsAvailable,
                                @RequestParam(required = false) String expiryDate) {
        BloodBank bank = new BloodBank();
        bank.setName(name);
        bank.setCity(city);
        bank.setAddress(address);
        bank.setPhone(phone);
        bank.setBloodGroup(bloodGroup);
        bank.setUnitsAvailable(unitsAvailable);
        if (expiryDate != null && !expiryDate.isBlank()) {
            bank.setExpiryDate(java.time.LocalDate.parse(expiryDate));
        }
        bloodBanks.save(bank);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/blood-banks/{id}/stock")
    public String updateStock(@PathVariable Long id, @RequestParam int unitsAvailable) {
        bloodBanks.findById(id).ifPresent(bank -> {
            bank.setUnitsAvailable(unitsAvailable);
            bloodBanks.save(bank);
        });
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/requests/{id}/close")
    public String closeRequest(@PathVariable Long id) {
        requests.findById(id).ifPresent(r -> {
            r.setStatus(RequestStatus.CLOSED);
            requests.save(r);
        });
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/hospitals")
    public String addHospital(@RequestParam String name, @RequestParam String city,
                               @RequestParam(required = false) String area,
                               @RequestParam(required = false) Boolean hasBloodBank) {
        Hospital hospital = new Hospital();
        hospital.setName(name);
        hospital.setCity(city);
        hospital.setArea(area);
        hospital.setHasBloodBank(Boolean.TRUE.equals(hasBloodBank));

        double[] coords = CityCoordinates.lookup(city);
        hospital.setLatitude(coords != null ? coords[0] : 22.9734);
        hospital.setLongitude(coords != null ? coords[1] : 78.6569);

        hospitals.save(hospital);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/hospitals/{id}/delete")
    public String deleteHospital(@PathVariable Long id) {
        hospitals.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/donors/{id}/defer")
    public String deferDonor(@PathVariable Long id, @RequestParam String reason,
                              @RequestParam(required = false) String until) {
        donors.findById(id).ifPresent(d -> {
            d.setDeferred(true);
            d.setDeferReason(reason);
            d.setDeferredUntil((until == null || until.isBlank()) ? null : java.time.LocalDate.parse(until));
            donors.save(d);
        });
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/donors/{id}/undefer")
    public String undeferDonor(@PathVariable Long id) {
        donors.findById(id).ifPresent(d -> {
            d.setDeferred(false);
            d.setDeferReason(null);
            d.setDeferredUntil(null);
            donors.save(d);
        });
        return "redirect:/admin/dashboard";
    }
}
