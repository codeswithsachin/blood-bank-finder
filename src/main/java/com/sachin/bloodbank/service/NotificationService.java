package com.sachin.bloodbank.service;

import com.sachin.bloodbank.model.BloodRequest;
import com.sachin.bloodbank.model.Donor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void notifyDonor(Donor donor, BloodRequest request) {
        // Notification adapter point.
        // Add Twilio credentials and SDK here when SMS is enabled.
        log.info("MATCHED DONOR: {} | {} | request #{} | phone={}",
                donor.getName(), donor.getBloodGroup(), request.getId(), donor.getPhone());
    }
}
