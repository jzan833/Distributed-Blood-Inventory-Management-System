package com.redhope.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.redhope.entity.BloodRequest;

public class CriticalBroadcastDTO {

    private Long requestId;
    private String hospitalName;
    private String bloodType;
    private String bloodTypeDisplayName;
    private String city;
    private String urgency;
    private String urgencyDisplayName;
    private String message;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime deactivatedAt;
    private String createdAtFormatted;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    public CriticalBroadcastDTO() {
    }

    public CriticalBroadcastDTO(BloodRequest request) {
        this.requestId = request.getId();
        this.hospitalName = request.getHospital().getName();
        this.bloodType = request.getBloodType().name();
        this.bloodTypeDisplayName = request.getBloodType().getDisplayName();
        this.city = request.getHospital().getCity();
        this.urgency = request.getUrgency().name();
        this.urgencyDisplayName = request.getUrgency().getDisplayName();
        this.message = String.format("%s: %s blood needed urgently at %s in %s",
                request.getUrgency().getDisplayName(),
                request.getBloodType().getDisplayName(),
                request.getHospital().getName(),
                request.getHospital().getCity());
        this.active = true;
        this.createdAt = request.getRequestedAt();
        this.createdAtFormatted = createdAt != null ? createdAt.format(FORMATTER) : "";
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getBloodTypeDisplayName() {
        return bloodTypeDisplayName;
    }

    public void setBloodTypeDisplayName(String bloodTypeDisplayName) {
        this.bloodTypeDisplayName = bloodTypeDisplayName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getUrgencyDisplayName() {
        return urgencyDisplayName;
    }

    public void setUrgencyDisplayName(String urgencyDisplayName) {
        this.urgencyDisplayName = urgencyDisplayName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeactivatedAt() {
        return deactivatedAt;
    }

    public void setDeactivatedAt(LocalDateTime deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public String getCreatedAtFormatted() {
        return createdAtFormatted;
    }

    public void setCreatedAtFormatted(String createdAtFormatted) {
        this.createdAtFormatted = createdAtFormatted;
    }
}
