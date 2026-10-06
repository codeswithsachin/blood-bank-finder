package com.sachin.bloodbank.repository;

import com.sachin.bloodbank.model.DonationRecord;
import com.sachin.bloodbank.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRecordRepository extends JpaRepository<DonationRecord, Long> {
    List<DonationRecord> findByDonorOrderByDonationDateDesc(Donor donor);
}
