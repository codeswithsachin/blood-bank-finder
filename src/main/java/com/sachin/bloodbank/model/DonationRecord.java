package com.sachin.bloodbank.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "donation_records")
public class DonationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @Column(nullable = false)
    private LocalDate donationDate;

    private String location;

    public Long getId() { return id; }
    public Donor getDonor() { return donor; }
    public LocalDate getDonationDate() { return donationDate; }
    public String getLocation() { return location; }

    public void setDonor(Donor donor) { this.donor = donor; }
    public void setDonationDate(LocalDate donationDate) { this.donationDate = donationDate; }
    public void setLocation(String location) { this.location = location; }
}
