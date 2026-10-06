package com.sachin.bloodbank.repository;

import com.sachin.bloodbank.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
}
