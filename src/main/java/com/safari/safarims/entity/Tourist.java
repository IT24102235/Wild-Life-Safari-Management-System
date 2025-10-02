package com.safari.safarims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "tourists")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tourist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "dietary_preferences")
    private String dietaryPreferences;

    // New relationship for preferred languages
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "tourist_preferred_languages",
        joinColumns = @JoinColumn(name = "tourist_id"),
        inverseJoinColumns = @JoinColumn(name = "language_id")
    )
    private Set<Language> preferredLanguages;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
