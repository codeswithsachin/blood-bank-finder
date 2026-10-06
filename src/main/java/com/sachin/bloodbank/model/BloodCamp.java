package com.sachin.bloodbank.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "blood_camps")
public class BloodCamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    private String area;

    @Column(nullable = false)
    private LocalDate campDate;

    private String organizer;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getArea() { return area; }
    public LocalDate getCampDate() { return campDate; }
    public String getOrganizer() { return organizer; }

    public void setName(String name) { this.name = name; }
    public void setCity(String city) { this.city = city; }
    public void setArea(String area) { this.area = area; }
    public void setCampDate(LocalDate campDate) { this.campDate = campDate; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
}
