package com.sachin.bloodbank.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "blood_banks")
public class BloodBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    private String address;
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private int unitsAvailable;

    /** Soonest expiry date across this stock line's units, if tracked. */
    private LocalDate expiryDate;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public BloodGroup getBloodGroup() { return bloodGroup; }
    public int getUnitsAvailable() { return unitsAvailable; }
    public LocalDate getExpiryDate() { return expiryDate; }

    public void setName(String name) { this.name = name; }
    public void setCity(String city) { this.city = city; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setBloodGroup(BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    public void setUnitsAvailable(int unitsAvailable) { this.unitsAvailable = unitsAvailable; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}
