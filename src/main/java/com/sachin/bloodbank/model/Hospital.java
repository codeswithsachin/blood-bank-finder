package com.sachin.bloodbank.model;

import jakarta.persistence.*;

@Entity
@Table(name = "hospitals")
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    private String area;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private boolean hasBloodBank;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getArea() { return area; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isHasBloodBank() { return hasBloodBank; }

    public void setName(String name) { this.name = name; }
    public void setCity(String city) { this.city = city; }
    public void setArea(String area) { this.area = area; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setHasBloodBank(boolean hasBloodBank) { this.hasBloodBank = hasBloodBank; }
}
