package com.sachin.bloodbank.service;

import com.sachin.bloodbank.model.*;
import com.sachin.bloodbank.repository.DonationRecordRepository;
import com.sachin.bloodbank.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class DonorService {
    private final DonorRepository repository;
    private final DonationRecordRepository donationRecords;
    private final long donationGapDays;

    public DonorService(DonorRepository repository,
                        DonationRecordRepository donationRecords,
                        @Value("${app.donation-gap-days:90}") long donationGapDays) {
        this.repository = repository;
        this.donationRecords = donationRecords;
        this.donationGapDays = donationGapDays;
    }

    public Donor register(Donor donor) {
        return repository.save(donor);
    }

    public List<Donor> search(BloodGroup group, String city) {
        return repository.findByBloodGroupAndCityIgnoreCaseAndActiveTrue(group, city)
                .stream()
                .filter(d -> d.isEligible(donationGapDays))
                .toList();
    }

    public Donor updateDonation(Long id, LocalDate date, String location) {
        Donor donor = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));
        donor.setLastDonationDate(date);
        repository.save(donor);

        DonationRecord record = new DonationRecord();
        record.setDonor(donor);
        record.setDonationDate(date);
        record.setLocation(location);
        donationRecords.save(record);

        return donor;
    }

    public List<DonationRecord> historyFor(Donor donor) {
        return donationRecords.findByDonorOrderByDonationDateDesc(donor);
    }

    public Donor updateProfile(Long id, String city, String phone, boolean active) {
        Donor donor = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));
        donor.setCity(city);
        donor.setPhone(phone);
        donor.setActive(active);
        return repository.save(donor);
    }

    public long getDonationGapDays() {
        return donationGapDays;
    }
}
