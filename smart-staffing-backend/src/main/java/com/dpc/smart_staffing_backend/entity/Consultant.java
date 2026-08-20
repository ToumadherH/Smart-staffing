package com.dpc.smart_staffing_backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "consultants")
public class Consultant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(name = "years_of_experience", nullable = false)
    private Integer yearsOfExperience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Availability availability;

    @Column(name = "current_mission")
    private String currentMission;

    private String location;

    // A simple list of language names (e.g. "French", "English").
    // @ElementCollection stores these in their own table since they aren't a full entity on their own.
    @ElementCollection
    @CollectionTable(name = "consultant_languages", joinColumns = @JoinColumn(name = "consultant_id"))
    @Column(name = "language", nullable = false)
    private List<String> languages = new ArrayList<>();

    // Consultant owns the relationship: it declares the join table.
    // A Set (not a List) prevents the same skill from being attached twice.
    @ManyToMany
    @JoinTable(
            name = "consultant_skills",
            joinColumns = @JoinColumn(name = "consultant_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    // A CV is private to one consultant and is removed with that consultant.
    @OneToOne(mappedBy = "consultant", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private Cv cv;

    protected Consultant() {
    }

    public Consultant(String name, String email, String phone, Integer yearsOfExperience,
                       Availability availability, String currentMission, String location) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.yearsOfExperience = yearsOfExperience;
        this.availability = availability;
        this.currentMission = currentMission;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public String getCurrentMission() {
        return currentMission;
    }

    public void setCurrentMission(String currentMission) {
        this.currentMission = currentMission;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public void setSkills(Set<Skill> skills) {
        this.skills = skills;
    }

    public Cv getCv() {
        return cv;
    }

    public void setCv(Cv cv) {
        this.cv = cv;
    }
}
