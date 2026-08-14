package com.redhope.dto;

public class StepperStatusDTO {

    private final int currentStep;
    private final String currentStatus;
    private final boolean showStepper;
    private final String message;

    public StepperStatusDTO(int currentStep, String currentStatus, boolean showStepper, String message) {
        this.currentStep = currentStep;
        this.currentStatus = currentStatus;
        this.showStepper = showStepper;
        this.message = message;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public boolean isShowStepper() {
        return showStepper;
    }

    public String getMessage() {
        return message;
    }
}
