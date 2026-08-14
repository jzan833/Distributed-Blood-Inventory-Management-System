package com.redhope.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.redhope.entity.User;
import com.redhope.enums.BloodType;

public class EligibilityDTO {

    private final boolean eligible;
    private final long daysUntilEligible;
    private final LocalDate lastDonationDate;
    private final LocalDate nextEligibleDate;
    private final long hoursUntilEligible;
    private final long minutesUntilEligible;
    private final long secondsUntilEligible;

    public EligibilityDTO(User user) {
        this.lastDonationDate = user.getLastDonationDate();

        if (this.lastDonationDate == null) {
            this.eligible = true;
            this.daysUntilEligible = 0;
            this.nextEligibleDate = null;
            this.hoursUntilEligible = 0;
            this.minutesUntilEligible = 0;
            this.secondsUntilEligible = 0;
        } else {
            LocalDate today = LocalDate.now();
            LocalDate eligibilityDate = this.lastDonationDate.plusDays(90);
            this.nextEligibleDate = eligibilityDate;

            if (today.isAfter(eligibilityDate) || today.isEqual(eligibilityDate)) {
                this.eligible = true;
                this.daysUntilEligible = 0;
                this.hoursUntilEligible = 0;
                this.minutesUntilEligible = 0;
                this.secondsUntilEligible = 0;
            } else {
                this.eligible = false;
                this.daysUntilEligible = ChronoUnit.DAYS.between(today, eligibilityDate);

                long totalSeconds = ChronoUnit.SECONDS.between(
                        today.atStartOfDay(), eligibilityDate.atStartOfDay());
                this.hoursUntilEligible = (totalSeconds % 86400) / 3600;
                this.minutesUntilEligible = (totalSeconds % 3600) / 60;
                this.secondsUntilEligible = totalSeconds % 60;
            }
        }
    }

    public boolean isEligible() {
        return eligible;
    }

    public long getDaysUntilEligible() {
        return daysUntilEligible;
    }

    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public LocalDate getNextEligibleDate() {
        return nextEligibleDate;
    }

    public long getHoursUntilEligible() {
        return hoursUntilEligible;
    }

    public long getMinutesUntilEligible() {
        return minutesUntilEligible;
    }

    public long getSecondsUntilEligible() {
        return secondsUntilEligible;
    }
}
