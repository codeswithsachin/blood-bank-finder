package com.sachin.bloodbank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false, length = 20)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private int unitsRequired;

    @Column(nullable = false)
    private String requesterName;

    @Column(nullable = false)
    private String requesterPhone;

    private String hospital;
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.OPEN;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public BloodGroup getBloodGroup() { return bloodGroup; }
    public String getCity() { return city; }
    public int getUnitsRequired() { return unitsRequired; }
    public String getRequesterName() { return requesterName; }
    public String getRequesterPhone() { return requesterPhone; }
    public String getHospital() { return hospital; }
    public String getMessage() { return message; }
    public RequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setBloodGroup(BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    public void setCity(String city) { this.city = city; }
    public void setUnitsRequired(int unitsRequired) { this.unitsRequired = unitsRequired; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public void setRequesterPhone(String requesterPhone) { this.requesterPhone = requesterPhone; }
    public void setHospital(String hospital) { this.hospital = hospital; }
    public void setMessage(String message) { this.message = message; }
    public void setStatus(RequestStatus status) { this.status = status; }
}
