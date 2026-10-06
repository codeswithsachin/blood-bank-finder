package com.sachin.bloodbank.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    // BCrypt-hashed. Never store or compare plain text passwords.
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // Only set when role == DONOR. Links the login account to the donor's profile record.
    @OneToOne
    @JoinColumn(name = "donor_id")
    private Donor donor;

    @Column(nullable = false)
    private boolean enabled = true;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public Donor getDonor() { return donor; }
    public boolean isEnabled() { return enabled; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setDonor(Donor donor) { this.donor = donor; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
