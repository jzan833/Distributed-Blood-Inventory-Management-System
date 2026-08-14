package com.redhope.event;

import com.redhope.entity.BloodRequest;

public class CriticalRequestEvent {

    private final BloodRequest bloodRequest;

    public CriticalRequestEvent(BloodRequest bloodRequest) {
        this.bloodRequest = bloodRequest;
    }

    public BloodRequest getBloodRequest() {
        return bloodRequest;
    }
}
