package com.redhope.entity;
import java.time.LocalDateTime;

import com.redhope.enums.BloodType;
import com.redhope.enums.RequestStatus;
import com.redhope.enums.Urgency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents a blood request submitted by a user to a specific hospital.
 */
@Entity
@Table(name = "blood_requests")
public class BloodRequest extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type", nullable = false, length = 20)
    private BloodType bloodType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", nullable = false, length = 20)
    private Urgency urgency = Urgency.NORMAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @NotBlank
    @Size(min = 20, max = 2000)
    @Column(name = "medical_reason", nullable = false, length = 2000)
    private String medicalReason;

    @Column(name = "doctor_referral_number", length = 100)
    private String doctorReferralNumber;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.requestedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public void setBloodType(BloodType bloodType) {
        this.bloodType = bloodType;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getMedicalReason() {
        return medicalReason;
    }

    public void setMedicalReason(String medicalReason) {
        this.medicalReason = medicalReason;
    }

    public String getDoctorReferralNumber() {
        return doctorReferralNumber;
    }

    public void setDoctorReferralNumber(String doctorReferralNumber) {
        this.doctorReferralNumber = doctorReferralNumber;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}
