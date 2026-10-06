package com.sachin.bloodbank.config;

import com.sachin.bloodbank.model.Hospital;
import com.sachin.bloodbank.repository.HospitalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds a starter directory of well-known Indian hospitals so the map/
 * directory feature has real data on first run. Names and cities are public
 * knowledge; coordinates are city-level approximations, not exact addresses.
 * Admins can add more via the database directly — there's no admin UI for
 * hospital management yet (see PROJECT_CONTEXT.md's "not yet built" list).
 */
@Component
public class HospitalSeeder implements CommandLineRunner {
    private final HospitalRepository hospitals;

    public HospitalSeeder(HospitalRepository hospitals) {
        this.hospitals = hospitals;
    }

    @Override
    public void run(String... args) {
        if (hospitals.count() > 0) return;

        seed("All India Institute of Medical Sciences (AIIMS)", "New Delhi", "Ansari Nagar", 28.5672, 77.2100, true);
        seed("Sir Ganga Ram Hospital", "New Delhi", "Rajinder Nagar", 28.6406, 77.1900, true);
        seed("Tata Memorial Hospital", "Mumbai", "Parel", 19.0018, 72.8427, true);
        seed("Seth GS Medical College & KEM Hospital", "Mumbai", "Parel", 19.0022, 72.8416, true);
        seed("Christian Medical College (CMC)", "Vellore", "Ida Scudder Road", 12.9244, 79.1350, true);
        seed("NIMHANS", "Bangalore", "Hosur Road", 12.9432, 77.5960, false);
        seed("Post Graduate Institute of Medical Education and Research (PGIMER)", "Chandigarh", "Sector 12", 30.7650, 76.7730, true);
        seed("Apollo Hospitals", "Chennai", "Greams Road", 13.0604, 80.2496, true);
        seed("SSKM Hospital (IPGMER)", "Kolkata", "Bhowanipore", 22.5354, 88.3428, true);
        seed("Nizam's Institute of Medical Sciences (NIMS)", "Hyderabad", "Panjagutta", 17.4239, 78.4482, true);
        seed("B.J. Government Medical College & Sassoon Hospital", "Pune", "Sassoon Road", 18.5289, 73.8697, true);
        seed("Civil Hospital", "Ahmedabad", "Asarwa", 23.0432, 72.6023, true);
        seed("Sir Sayajirao General (SSG) Hospital", "Vadodara", "Anandpura", 22.3126, 73.2003, true);
        seed("Sawai Man Singh (SMS) Hospital", "Jaipur", "JLN Marg", 26.9089, 75.8055, true);
        seed("King George's Medical University (KGMU)", "Lucknow", "Chowk", 26.8709, 80.9199, true);
    }

    private void seed(String name, String city, String area, double lat, double lng, boolean hasBloodBank) {
        Hospital h = new Hospital();
        h.setName(name);
        h.setCity(city);
        h.setArea(area);
        h.setLatitude(lat);
        h.setLongitude(lng);
        h.setHasBloodBank(hasBloodBank);
        hospitals.save(h);
    }
}
