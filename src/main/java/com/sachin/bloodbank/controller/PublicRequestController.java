package com.sachin.bloodbank.controller;

import com.sachin.bloodbank.model.BloodGroup;
import com.sachin.bloodbank.model.BloodRequest;
import com.sachin.bloodbank.service.BloodRequestService;
import com.sachin.bloodbank.util.CaptchaSupport;
import com.sachin.bloodbank.util.PhoneValidator;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PublicRequestController {
    private final BloodRequestService requestService;

    public PublicRequestController(BloodRequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/request-blood")
    public String form(Model model, HttpSession session) {
        model.addAttribute("bloodGroups", BloodGroup.values());
        CaptchaSupport.generateChallenge(session, model);
        return "request-blood";
    }

    @PostMapping("/request-blood")
    public String submit(@RequestParam BloodGroup bloodGroup, @RequestParam String city,
                          @RequestParam int unitsRequired, @RequestParam String requesterName,
                          @RequestParam String requesterPhone, @RequestParam String hospital,
                          @RequestParam(required = false) String message,
                          @RequestParam(name = "captchaAnswer") String captchaAnswer,
                          Model model, HttpSession session) {
        model.addAttribute("bloodGroups", BloodGroup.values());

        if (!CaptchaSupport.verify(session, captchaAnswer)) {
            model.addAttribute("error", "That verification answer wasn't correct. Please try again.");
            CaptchaSupport.generateChallenge(session, model);
            return "request-blood";
        }
        if (!PhoneValidator.isValid(requesterPhone)) {
            model.addAttribute("error", "Please enter a valid 10-digit Indian mobile number.");
            CaptchaSupport.generateChallenge(session, model);
            return "request-blood";
        }

        BloodRequest request = new BloodRequest();
        request.setBloodGroup(bloodGroup);
        request.setCity(city);
        request.setUnitsRequired(unitsRequired);
        request.setRequesterName(requesterName);
        request.setRequesterPhone(requesterPhone);
        request.setHospital(hospital);
        request.setMessage(message);
        requestService.create(request);

        model.addAttribute("success", true);
        CaptchaSupport.generateChallenge(session, model);
        return "request-blood";
    }
}
