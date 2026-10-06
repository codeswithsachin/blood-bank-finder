package com.sachin.bloodbank.service;

import com.sachin.bloodbank.model.*;
import com.sachin.bloodbank.repository.BloodRequestRepository;
import com.sachin.bloodbank.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BloodRequestService {
    private final BloodRequestRepository requests;
    private final DonorRepository donors;
    private final NotificationService notifications;
    private final long donationGapDays;

    public BloodRequestService(BloodRequestRepository requests,
                               DonorRepository donors,
                               NotificationService notifications,
                               @Value("${app.donation-gap-days:90}") long donationGapDays) {
        this.requests = requests;
        this.donors = donors;
        this.notifications = notifications;
        this.donationGapDays = donationGapDays;
    }

    public BloodRequest create(BloodRequest request) {
        BloodRequest saved = requests.save(request);

        donors.findByBloodGroupAndCityIgnoreCaseAndActiveTrue(saved.getBloodGroup(), saved.getCity())
                .stream()
                .filter(d -> d.isEligible(donationGapDays))
                .forEach(d -> notifications.notifyDonor(d, saved));

        return saved;
    }

    public List<BloodRequest> openRequests() {
        return requests.findByStatusOrderByCreatedAtDesc(RequestStatus.OPEN);
    }

    public BloodRequest close(Long id) {
        BloodRequest request = requests.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        request.setStatus(RequestStatus.CLOSED);
        return requests.save(request);
    }
}
