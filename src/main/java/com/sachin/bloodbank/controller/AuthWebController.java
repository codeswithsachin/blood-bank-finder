package com.sachin.bloodbank.controller;

import com.sachin.bloodbank.model.BloodGroup;
import com.sachin.bloodbank.model.Donor;
import com.sachin.bloodbank.service.UserService;
import com.sachin.bloodbank.util.CaptchaSupport;
import com.sachin.bloodbank.util.PhoneValidator;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthWebController {
    private final UserService userService;

    public AuthWebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        model.addAttribute("bloodGroups", BloodGroup.values());
        CaptchaSupport.generateChallenge(session, model);
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                            @RequestParam String password,
                            @RequestParam String name,
                            @RequestParam BloodGroup bloodGroup,
                            @RequestParam String city,
                            @RequestParam String phone,
                            @RequestParam(name = "captchaAnswer") String captchaAnswer,
                            Model model, HttpSession session) {
        model.addAttribute("bloodGroups", BloodGroup.values());

        if (!CaptchaSupport.verify(session, captchaAnswer)) {
            model.addAttribute("error", "That verification answer wasn't correct. Please try again.");
            CaptchaSupport.generateChallenge(session, model);
            return "register";
        }
        if (!PhoneValidator.isValid(phone)) {
            model.addAttribute("error", "Please enter a valid 10-digit Indian mobile number.");
            CaptchaSupport.generateChallenge(session, model);
            return "register";
        }

        try {
            Donor donor = new Donor();
            donor.setName(name);
            donor.setBloodGroup(bloodGroup);
            donor.setCity(city);
            donor.setPhone(phone);
            donor.setActive(true);

            userService.registerDonor(username, password, donor);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            CaptchaSupport.generateChallenge(session, model);
            return "register";
        }
    }
}
