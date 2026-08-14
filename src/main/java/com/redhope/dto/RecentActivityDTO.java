package com.redhope.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.redhope.enums.BloodType;
import com.redhope.enums.DonationStatus;
import com.redhope.enums.RequestStatus;
import com.redhope.enums.Urgency;

public class RecentActivityDTO {

    private final String type;
    private final Long id;
    private final String hospitalName;
    private final String bloodTypeDisplayName;
    private final String status;
    private final String statusDisplayName;
    private final String urgency;
    private final String urgencyDisplayName;
    private final String date;
    private final String preferredDate;
    private final String statusBadgeClass;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public RecentActivityDTO(String type, Long id, String hospitalName,
                             BloodType bloodType, RequestStatus requestStatus,
                             Urgency urgency, LocalDateTime requestedAt) {
        this.type = "request";
        this.id = id;
        this.hospitalName = hospitalName;
        this.bloodTypeDisplayName = bloodType != null ? bloodType.getDisplayName() : "";
        this.status = requestStatus != null ? requestStatus.name() : "";
        this.statusDisplayName = requestStatus != null ? requestStatus.getDisplayName() : "";
        this.urgency = urgency != null ? urgency.name() : "";
        this.urgencyDisplayName = urgency != null ? urgency.getDisplayName() : "";
        this.date = requestedAt != null ? requestedAt.format(FORMATTER) : "";
        this.preferredDate = "";
        this.statusBadgeClass = getStatusBadgeClass(requestStatus);
    }

    public RecentActivityDTO(String type, Long id, String hospitalName,
                             LocalDate preferredDate, DonationStatus donationStatus,
                             LocalDateTime requestedAt) {
        this.type = "donation";
        this.id = id;
        this.hospitalName = hospitalName;
        this.bloodTypeDisplayName = "";
        this.status = donationStatus != null ? donationStatus.name() : "";
        this.statusDisplayName = donationStatus != null ? donationStatus.getDisplayName() : "";
        this.urgency = "";
        this.urgencyDisplayName = "";
        this.date = requestedAt != null ? requestedAt.format(FORMATTER) : "";
        this.preferredDate = preferredDate != null ? preferredDate.format(FORMATTER) : "";
        this.statusBadgeClass = getStatusBadgeClass(donationStatus);
    }

    private String getStatusBadgeClass(RequestStatus status) {
        if (status == null) {
            return "bg-secondary";
        }
        switch (status) {
            case PENDING:
                return "bg-warning text-dark";
            case APPROVED:
                return "bg-info";
            case COMPLETED:
                return "bg-success";
            case REJECTED:
            case CANCELLED:
                return "bg-danger";
            default:
                return "bg-secondary";
        }
    }

    private String getStatusBadgeClass(DonationStatus status) {
        if (status == null) {
            return "bg-secondary";
        }
        switch (status) {
            case PENDING:
                return "bg-warning text-dark";
            case APPROVED:
                return "bg-info";
            case COMPLETED:
                return "bg-success";
            case REJECTED:
            case CANCELLED:
                return "bg-danger";
            default:
                return "bg-secondary";
        }
    }

    public String getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public String getBloodTypeDisplayName() {
        return bloodTypeDisplayName;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusDisplayName() {
        return statusDisplayName;
    }

    public String getUrgency() {
        return urgency;
    }

    public String getUrgencyDisplayName() {
        return urgencyDisplayName;
    }

    public String getDate() {
        return date;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public String getStatusBadgeClass() {
        return statusBadgeClass;
    }
}
