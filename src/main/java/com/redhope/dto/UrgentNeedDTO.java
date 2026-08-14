package com.redhope.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.redhope.enums.BloodType;
import com.redhope.enums.Urgency;

public class UrgentNeedDTO {

    private final String hospitalName;
    private final String bloodType;
    private final String bloodTypeDisplayName;
    private final String urgency;
    private final String urgencyDisplayName;
    private final String urgencyCssClass;
    private final String requestedAt;
    private final String urgencyBadgeClass;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public UrgentNeedDTO(Long id, String hospitalName, BloodType bloodType,
                         Urgency urgency, LocalDateTime requestedAt) {
        this.hospitalName = hospitalName;
        this.bloodType = bloodType.name();
        this.bloodTypeDisplayName = bloodType.getDisplayName();
        this.urgency = urgency.name();
        this.urgencyDisplayName = urgency.getDisplayName();
        this.requestedAt = requestedAt != null ? requestedAt.format(FORMATTER) : "";

        if (urgency == Urgency.CRITICAL) {
            this.urgencyCssClass = "bg-danger";
            this.urgencyBadgeClass = "bg-danger";
        } else if (urgency == Urgency.HIGH) {
            this.urgencyCssClass = "bg-warning text-dark";
            this.urgencyBadgeClass = "bg-warning text-dark";
        } else {
            this.urgencyCssClass = "bg-info";
            this.urgencyBadgeClass = "bg-info";
        }
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public String getBloodType() {
        return bloodType;
    }

    public String getBloodTypeDisplayName() {
        return bloodTypeDisplayName;
    }

    public String getUrgency() {
        return urgency;
    }

    public String getUrgencyDisplayName() {
        return urgencyDisplayName;
    }

    public String getUrgencyCssClass() {
        return urgencyCssClass;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public String getUrgencyBadgeClass() {
        return urgencyBadgeClass;
    }
}
