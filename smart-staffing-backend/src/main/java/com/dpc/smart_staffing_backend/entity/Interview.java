package com.dpc.smart_staffing_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    // Stored as a simple "HH:mm" string, matching the class diagram.
    @Column(nullable = false)
    private String time;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consultant_id", nullable = false)
    private Consultant consultant;

    // Optional: an interview may be tied to a specific staffing request.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staffing_request_id")
    private StaffingRequest staffingRequest;

    protected Interview() {
    }

    public Interview(LocalDate date, String time, String location, String notes,
                     Consultant consultant, StaffingRequest staffingRequest) {
        this.date = date;
        this.time = time;
        this.location = location;
        this.notes = notes;
        this.consultant = consultant;
        this.staffingRequest = staffingRequest;
    }

    public Long getId() { return id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public InterviewStatus getStatus() { return status; }
    public void setStatus(InterviewStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Consultant getConsultant() { return consultant; }
    public void setConsultant(Consultant consultant) { this.consultant = consultant; }

    public StaffingRequest getStaffingRequest() { return staffingRequest; }
    public void setStaffingRequest(StaffingRequest staffingRequest) { this.staffingRequest = staffingRequest; }
}
