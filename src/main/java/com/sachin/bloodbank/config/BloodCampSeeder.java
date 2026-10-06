package com.sachin.bloodbank.config;

import com.sachin.bloodbank.model.BloodCamp;
import com.sachin.bloodbank.repository.BloodCampRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds a few sample upcoming blood donation camps on first startup, purely
 * as demo/placeholder data (not real scheduled events) — dates are computed
 * relative to whenever the app first starts, so they always show as
 * "upcoming" rather than going stale with a hardcoded past date. An admin
 * UI for managing real camps doesn't exist yet (see PROJECT_CONTEXT.md).
 */
@Component
public class BloodCampSeeder implements CommandLineRunner {
    private final BloodCampRepository camps;

    public BloodCampSeeder(BloodCampRepository camps) {
        this.camps = camps;
    }

    @Override
    public void run(String... args) {
        if (camps.count() > 0) return;

        LocalDate today = LocalDate.now();
        seed("Community Blood Donation Drive", "Vadodara", "Alkapuri", today.plusDays(9), "Red Cross Society (local chapter)");
        seed("College Blood Donation Camp", "Ahmedabad", "Navrangpura", today.plusDays(16), "Local college NSS unit");
        seed("World Blood Donor Day Camp", "Mumbai", "Andheri", today.plusDays(23), "Municipal health department");
        seed("Corporate Blood Donation Camp", "Bangalore", "Whitefield", today.plusDays(30), "IT park CSR initiative");
    }

    private void seed(String name, String city, String area, LocalDate date, String organizer) {
        BloodCamp camp = new BloodCamp();
        camp.setName(name);
        camp.setCity(city);
        camp.setArea(area);
        camp.setCampDate(date);
        camp.setOrganizer(organizer);
        camps.save(camp);
    }
}
