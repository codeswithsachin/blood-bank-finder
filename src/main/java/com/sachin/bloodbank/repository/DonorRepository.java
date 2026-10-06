package com.sachin.bloodbank.repository;

import com.sachin.bloodbank.model.BloodGroup;
import com.sachin.bloodbank.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonorRepository extends JpaRepository<Donor, Long> {
    List<Donor> findByBloodGroupAndCityIgnoreCaseAndActiveTrue(BloodGroup bloodGroup, String city);
}
