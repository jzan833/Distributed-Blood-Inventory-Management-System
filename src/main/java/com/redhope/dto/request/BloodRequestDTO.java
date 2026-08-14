package com.redhope.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BloodRequestDTO {

    @NotNull(message = "Please select a hospital")
    private Long hospitalId;

    @NotBlank(message = "Please select a blood type")
    private String bloodType;

    @NotBlank(message = "Please select an urgency level")
    private String urgency;

    @NotBlank(message = "Medical reason is required")
    @Size(min = 20, max = 2000, message = "Medical reason must be between 20 and 2000 characters")
    private String medicalReason;

    @Size(max = 100, message = "Doctor referral number must not exceed 100 characters")
    private String doctorReferralNumber;

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
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
}
