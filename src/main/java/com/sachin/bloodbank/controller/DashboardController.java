package com.sachin.bloodbank.controller;

import com.sachin.bloodbank.model.*;
import com.sachin.bloodbank.repository.BloodRequestRepository;
import com.sachin.bloodbank.repository.UserRepository;
import com.sachin.bloodbank.service.BloodRequestService;
import com.sachin.bloodbank.service.DonorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {
    private final UserRepository users;
    private final BloodRequestRepository requests;
    private final DonorService donorService;
    private final BloodRequestService requestService;
    private final long donationGapDays;

    public DashboardController(UserRepository users, BloodRequestRepository requests,
                                DonorService donorService, BloodRequestService requestService,
                                @Value("${app.donation-gap-days:90}") long donationGapDays) {
        this.users = users;
        this.requests = requests;
        this.donorService = donorService;
        this.requestService = requestService;
        this.donationGapDays = donationGapDays;
    }

    private Donor currentDonor(Authentication auth) {
        User user = users.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));
        return user.getDonor();
    }

    @GetMapping("/dashboard")
    public String donorDashboard(Authentication auth, Model model) {
        Donor donor = currentDonor(auth);
        model.addAttribute("donor", donor);
        model.addAttribute("bloodGroups", BloodGroup.values());

        List<BloodRequest> matchingRequests = donor == null
                ? List.of()
                : requests.findByStatusOrderByCreatedAtDesc(RequestStatus.OPEN).stream()
                        .filter(r -> r.getBloodGroup() == donor.getBloodGroup()
                                && r.getCity().equalsIgnoreCase(donor.getCity()))
                        .toList();
        model.addAttribute("matchingRequests", matchingRequests);

        if (donor != null) {
            model.addAttribute("nextEligibleDate", donor.nextEligibleDate(donationGapDays));
            model.addAttribute("eligible", donor.isEligible(donationGapDays));
            model.addAttribute("donationHistory", donorService.historyFor(donor));
        }

        return "dashboard";
    }

    @PostMapping("/dashboard/log-donation")
    public String logDonation(Authentication auth, @RequestParam(required = false) String donationDate,
                               @RequestParam(required = false) String location) {
        Donor donor = currentDonor(auth);
        if (donor != null) {
            LocalDate date = (donationDate == null || donationDate.isBlank())
                    ? LocalDate.now() : LocalDate.parse(donationDate);
            donorService.updateDonation(donor.getId(), date, location);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/profile")
    public String updateProfile(Authentication auth, @RequestParam String city,
                                 @RequestParam String phone, @RequestParam(required = false) String active) {
        Donor donor = currentDonor(auth);
        if (donor != null) {
            donorService.updateProfile(donor.getId(), city, phone, active != null);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/request-blood")
    public String requestBlood(Authentication auth, @RequestParam BloodGroup bloodGroup,
                                @RequestParam String city, @RequestParam int unitsRequired,
                                @RequestParam String hospital, @RequestParam(required = false) String message) {
        Donor donor = currentDonor(auth);
        BloodRequest request = new BloodRequest();
        request.setBloodGroup(bloodGroup);
        request.setCity(city);
        request.setUnitsRequired(unitsRequired);
        request.setRequesterName(donor != null ? donor.getName() : auth.getName());
        request.setRequesterPhone(donor != null ? donor.getPhone() : "N/A");
        request.setHospital(hospital);
        request.setMessage(message);
        requestService.create(request);
        return "redirect:/dashboard";
    }
}
