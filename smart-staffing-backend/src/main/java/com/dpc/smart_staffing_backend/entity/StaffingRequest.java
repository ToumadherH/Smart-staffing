package com.dpc.smart_staffing_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "staffing_requests")
public class StaffingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    private String location;

    @Column(name = "years_of_experience_required")
    private Integer yearsOfExperienceRequired;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffingRequestStatus status = StaffingRequestStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @ManyToMany
    @JoinTable(
            name = "staffing_request_skills",
            joinColumns = @JoinColumn(name = "staffing_request_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> requiredSkills = new HashSet<>();

    protected StaffingRequest() {
    }

    public StaffingRequest(String title, String clientName, String location,
                           Integer yearsOfExperienceRequired, String description, StaffingRequestStatus status) {
        this.title = title;
        this.clientName = clientName;
        this.location = location;
        this.yearsOfExperienceRequired = yearsOfExperienceRequired;
        this.description = description;
        if (status != null) {
            this.status = status;
        }
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getYearsOfExperienceRequired() { return yearsOfExperienceRequired; }
    public void setYearsOfExperienceRequired(Integer yearsOfExperienceRequired) { this.yearsOfExperienceRequired = yearsOfExperienceRequired; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public StaffingRequestStatus getStatus() { return status; }
    public void setStatus(StaffingRequestStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Set<Skill> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(Set<Skill> requiredSkills) { this.requiredSkills = requiredSkills; }
}
