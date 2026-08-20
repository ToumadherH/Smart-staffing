package com.dpc.smart_staffing_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "cvs")
public class Cv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    // Internal generated name only; the client never receives this filesystem value.
    @Column(nullable = false, unique = true)
    private String storedFileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Instant uploadedAt;

    @OneToOne(optional = false)
    @JoinColumn(name = "consultant_id", nullable = false, unique = true)
    private Consultant consultant;

    protected Cv() {
    }

    public Cv(String fileName, String storedFileName, String contentType, Instant uploadedAt, Consultant consultant) {
        this.fileName = fileName;
        this.storedFileName = storedFileName;
        this.contentType = contentType;
        this.uploadedAt = uploadedAt;
        this.consultant = consultant;
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getStoredFileName() { return storedFileName; }
    public String getContentType() { return contentType; }
    public Instant getUploadedAt() { return uploadedAt; }
    public Consultant getConsultant() { return consultant; }
}
