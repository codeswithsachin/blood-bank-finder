package com.sachin.bloodbank.service;

import com.sachin.bloodbank.model.*;
import com.sachin.bloodbank.repository.DonorRepository;
import com.sachin.bloodbank.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository users;
    private final DonorRepository donors;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, DonorRepository donors, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.donors = donors;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerDonor(String username, String rawPassword, Donor donorDetails) {
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }

        Donor savedDonor = donors.save(donorDetails);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.DONOR);
        user.setDonor(savedDonor);
        return users.save(user);
    }
}
