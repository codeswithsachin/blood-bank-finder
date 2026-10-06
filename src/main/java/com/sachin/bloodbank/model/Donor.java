package com.sachin.bloodbank.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "donors", indexes = {
        @Index(name = "idx_donor_group_city", columnList = "blood_group,city")
})
public class Donor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false, length = 20)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String phone;

    private LocalDate lastDonationDate;

    @Column(nullable = false)
    private boolean active = true;

    // Donor deferment (temporary or permanent) — a donor may be deferred from
    // donating for health or eligibility reasons. A common real-world feature
    // in blood bank systems, so deferred donors don't get matched/notified
    // until their deferment period ends.
    @Column(nullable = false)
    private boolean deferred = false;

    private LocalDate deferredUntil; // null = permanent deferment
    private String deferReason;

    public Long getId() { return id; }
    public String getName() { return name; }
    public BloodGroup getBloodGroup() { return bloodGroup; }
    public String getCity() { return city; }
    public String getPhone() { return phone; }
    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public boolean isActive() { return active; }
    public boolean isDeferred() { return deferred; }
    public LocalDate getDeferredUntil() { return deferredUntil; }
    public String getDeferReason() { return deferReason; }

    public void setName(String name) { this.name = name; }
    public void setBloodGroup(BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    public void setCity(String city) { this.city = city; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }
    public void setActive(boolean active) { this.active = active; }
    public void setDeferred(boolean deferred) { this.deferred = deferred; }
    public void setDeferredUntil(LocalDate deferredUntil) { this.deferredUntil = deferredUntil; }
    public void setDeferReason(String deferReason) { this.deferReason = deferReason; }

    /** True if a deferment is currently in effect (permanent, or until-date not yet passed). */
    public boolean isCurrentlyDeferred() {
        if (!deferred) return false;
        return deferredUntil == null || !deferredUntil.isBefore(LocalDate.now());
    }

    /**
     * Single source of truth for donor eligibility — used by DonorService,
     * BloodRequestService, and DashboardController so the rule only lives in
     * one place. Checks both the donation-gap rule and deferment status.
     */
    public boolean isEligible(long donationGapDays) {
        if (isCurrentlyDeferred()) return false;
        return lastDonationDate == null || !lastDonationDate.isAfter(LocalDate.now().minusDays(donationGapDays));
    }

    public LocalDate nextEligibleDate(long donationGapDays) {
        return lastDonationDate == null ? LocalDate.now() : lastDonationDate.plusDays(donationGapDays);
    }
}
