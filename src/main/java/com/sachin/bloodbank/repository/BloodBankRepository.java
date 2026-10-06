package com.sachin.bloodbank.repository;

import com.sachin.bloodbank.model.BloodBank;
import com.sachin.bloodbank.model.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BloodBankRepository extends JpaRepository<BloodBank, Long> {
    List<BloodBank> findByBloodGroupAndCityIgnoreCase(BloodGroup bloodGroup, String city);
}
