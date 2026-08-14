package com.redhope.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public class DonationDTO {

    @NotNull(message = "Please select a hospital")
    private Long hospitalId;

    @NotNull(message = "Please select a preferred donation date")
    @Future(message = "Preferred date must be a future date (tomorrow or later)")
    private LocalDate preferredDate;

    @AssertTrue(message = "You must pass all health checklist items to donate")
    private Boolean healthChecklistPassed;

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public LocalDate getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(LocalDate preferredDate) {
        this.preferredDate = preferredDate;
    }

    public Boolean getHealthChecklistPassed() {
        return healthChecklistPassed;
    }

    public void setHealthChecklistPassed(Boolean healthChecklistPassed) {
        this.healthChecklistPassed = healthChecklistPassed;
    }
}
