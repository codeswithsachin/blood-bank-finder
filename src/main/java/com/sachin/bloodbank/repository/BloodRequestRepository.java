package com.sachin.bloodbank.repository;

import com.sachin.bloodbank.model.BloodRequest;
import com.sachin.bloodbank.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
}
