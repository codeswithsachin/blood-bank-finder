package com.sachin.bloodbank.repository;

import com.sachin.bloodbank.model.BloodCamp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BloodCampRepository extends JpaRepository<BloodCamp, Long> {
    List<BloodCamp> findByCampDateGreaterThanEqualOrderByCampDateAsc(LocalDate from);
}
