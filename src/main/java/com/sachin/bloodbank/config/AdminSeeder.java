package com.sachin.bloodbank.config;

import com.sachin.bloodbank.model.Role;
import com.sachin.bloodbank.model.User;
import com.sachin.bloodbank.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the admin login account on first startup so there's a way into
 * /admin/dashboard without touching the database by hand. Controlled by
 * app.admin-username / app.admin-password in application.yml (or env vars
 * ADMIN_USERNAME / ADMIN_PASSWORD).
 */
@Component
public class AdminSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminSeeder(UserRepository users, PasswordEncoder passwordEncoder,
                        @Value("${app.admin-username:admin}") String adminUsername,
                        @Value("${app.admin-password:admin123}") String adminPassword) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (users.existsByUsername(adminUsername)) {
            return;
        }
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        users.save(admin);
        log.info("Seeded admin login account '{}'. Change ADMIN_PASSWORD in production.", adminUsername);
    }
}
