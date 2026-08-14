package com.redhope.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redhope.dto.EligibilityDTO;
import com.redhope.dto.RecentActivityDTO;
import com.redhope.dto.StepperStatusDTO;
import com.redhope.dto.UrgentNeedDTO;
import com.redhope.entity.BloodDonation;
import com.redhope.entity.BloodInventory;
import com.redhope.entity.BloodRequest;
import com.redhope.entity.Hospital;
import com.redhope.entity.User;
import com.redhope.enums.DonationStatus;
import com.redhope.enums.RequestStatus;
import com.redhope.enums.Urgency;
import com.redhope.repository.BloodDonationRepository;
import com.redhope.repository.BloodInventoryRepository;
import com.redhope.repository.BloodRequestRepository;
import com.redhope.repository.HospitalRepository;
import com.redhope.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final BloodRequestRepository bloodRequestRepository;
    private final BloodDonationRepository bloodDonationRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodInventoryRepository bloodInventoryRepository;
    private final UserRepository userRepository;

    public DashboardService(BloodRequestRepository bloodRequestRepository,
                            BloodDonationRepository bloodDonationRepository,
                            HospitalRepository hospitalRepository,
                            BloodInventoryRepository bloodInventoryRepository,
                            UserRepository userRepository) {
        this.bloodRequestRepository = bloodRequestRepository;
        this.bloodDonationRepository = bloodDonationRepository;
        this.hospitalRepository = hospitalRepository;
        this.bloodInventoryRepository = bloodInventoryRepository;
        this.userRepository = userRepository;
    }

    public EligibilityDTO getEligibility(User user) {
        return new EligibilityDTO(user);
    }

    public List<UrgentNeedDTO> getUrgentNeeds(User user) {
        List<Urgency> urgencies = List.of(Urgency.CRITICAL, Urgency.HIGH);
        List<RequestStatus> statuses = List.of(RequestStatus.PENDING, RequestStatus.APPROVED);

        return bloodRequestRepository.findUrgentNeedsByBloodTypeAndCity(
                urgencies, user.getBloodType(), user.getCity(), statuses)
                .stream()
                .map(br -> new UrgentNeedDTO(
                        br.getId(),
                        br.getHospital().getName(),
                        br.getBloodType(),
                        br.getUrgency(),
                        br.getRequestedAt()))
                .collect(Collectors.toList());
    }

    public List<RecentActivityDTO> getRecentActivity(User user) {
        List<RecentActivityDTO> activities = new ArrayList<>();

        List<BloodRequest> recentRequests = bloodRequestRepository
                .findByRequesterOrderByRequestedAtDesc(user, PageRequest.of(0, 5))
                .getContent();
        for (BloodRequest br : recentRequests) {
            activities.add(new RecentActivityDTO(
                    "request",
                    br.getId(),
                    br.getHospital().getName(),
                    br.getBloodType(),
                    br.getStatus(),
                    br.getUrgency(),
                    br.getRequestedAt()));
        }

        List<BloodDonation> recentDonations = bloodDonationRepository
                .findByDonorOrderByRequestedAtDesc(user)
                .stream()
                .limit(5)
                .collect(Collectors.toList());
        for (BloodDonation bd : recentDonations) {
            activities.add(new RecentActivityDTO(
                    "donation",
                    bd.getId(),
                    bd.getHospital().getName(),
                    bd.getPreferredDate(),
                    bd.getStatus(),
                    bd.getRequestedAt()));
        }

        activities.sort(Comparator.comparing((RecentActivityDTO a) -> {
            if ("request".equals(a.getType())) {
                return a.getDate();
            } else {
                return a.getPreferredDate();
            }
        }).reversed());

        return activities.stream().limit(5).collect(Collectors.toList());
    }

    public StepperStatusDTO getStatusStepperData(User user) {
        List<BloodRequest> recentRequests = bloodRequestRepository
                .findByRequesterOrderByRequestedAtDesc(user, PageRequest.of(0, 1))
                .getContent();

        List<BloodDonation> recentDonations = bloodDonationRepository
                .findByDonorOrderByRequestedAtDesc(user);

        BloodRequest latestRequest = recentRequests.isEmpty() ? null : recentRequests.get(0);
        BloodDonation latestDonation = recentDonations.isEmpty() ? null : recentDonations.get(0);

        LocalDateTime latestRequestTime = latestRequest != null ? latestRequest.getRequestedAt() : null;
        LocalDateTime latestDonationTime = latestDonation != null ? latestDonation.getRequestedAt() : null;

        Object latestEntity = null;
        String entityType = null;

        if (latestRequestTime != null && latestDonationTime != null) {
            if (latestRequestTime.isAfter(latestDonationTime) || latestRequestTime.isEqual(latestDonationTime)) {
                latestEntity = latestRequest;
                entityType = "request";
            } else {
                latestEntity = latestDonation;
                entityType = "donation";
            }
        } else if (latestRequestTime != null) {
            latestEntity = latestRequest;
            entityType = "request";
        } else if (latestDonationTime != null) {
            latestEntity = latestDonation;
            entityType = "donation";
        }

        if (latestEntity == null) {
            return new StepperStatusDTO(0, "NONE", false,
                    "No requests or donations found. Submit a blood request or schedule a donation to see your status here.");
        }

        int step;
        String status;

        if (entityType.equals("request")) {
            RequestStatus reqStatus = ((BloodRequest) latestEntity).getStatus();
            status = reqStatus.name();
            switch (reqStatus) {
                case PENDING:
                    step = 1;
                    break;
                case APPROVED:
                    step = 2;
                    break;
                case COMPLETED:
                    step = 3;
                    break;
                case REJECTED:
                case CANCELLED:
                    step = -1;
                    break;
                default:
                    step = 1;
                    break;
            }
        } else {
            DonationStatus donStatus = ((BloodDonation) latestEntity).getStatus();
            status = donStatus.name();
            switch (donStatus) {
                case PENDING:
                    step = 1;
                    break;
                case APPROVED:
                    step = 2;
                    break;
                case COMPLETED:
                    step = 3;
                    break;
                case REJECTED:
                case CANCELLED:
                    step = -1;
                    break;
                default:
                    step = 1;
                    break;
            }
        }

        String message;
        if (step == -1) {
            message = entityType.equals("request")
                    ? "Most recent request was " + status.toLowerCase() + ". You can submit a new request."
                    : "Most recent donation was " + status.toLowerCase() + ". You can schedule a new donation.";
        } else if (step == 3) {
            message = entityType.equals("request")
                    ? "Your most recent request has been completed."
                    : "Your most recent donation has been completed.";
        } else {
            message = entityType.equals("request")
                    ? "Your most recent request is currently " + status.toLowerCase() + "."
                    : "Your most recent donation is currently " + status.toLowerCase() + ".";
        }

        return new StepperStatusDTO(step, status, true, message);
    }

    public boolean isEligibleToDonate(User user) {
        if (user.getLastDonationDate() == null) {
            return true;
        }
        LocalDate eligibilityDate = user.getLastDonationDate().plusDays(90);
        LocalDate today = LocalDate.now();
        return today.isAfter(eligibilityDate) || today.isEqual(eligibilityDate);
    }

    public long getDaysUntilEligible(User user) {
        if (isEligibleToDonate(user)) {
            return 0;
        }
        LocalDate eligibilityDate = user.getLastDonationDate().plusDays(90);
        return ChronoUnit.DAYS.between(LocalDate.now(), eligibilityDate);
    }

    public long getTotalRequests(Hospital hospital) {
        return bloodRequestRepository.countByHospital(hospital);
    }

    public long getTotalDonations(Hospital hospital) {
        return bloodDonationRepository.countByHospital(hospital);
    }

    public long getPendingRequests(Hospital hospital) {
        return bloodRequestRepository.countByHospitalAndStatus(hospital, RequestStatus.PENDING);
    }

    public long getPendingDonations(Hospital hospital) {
        return bloodDonationRepository.countByHospitalAndStatus(hospital, DonationStatus.PENDING);
    }

    public long getLowStockAlertsCount(Hospital hospital) {
        return bloodInventoryRepository.countByHospitalAndUnitsAvailableLessThan(hospital, 5);
    }

    public long getTodayAppointmentsCount(Hospital hospital) {
        LocalDate today = LocalDate.now();
        List<BloodDonation> appointments = bloodDonationRepository
                .findByHospitalAndPreferredDateBetweenOrderByPreferredDateAsc(hospital, today, today);
        return appointments.size();
    }

    public List<BloodRequest> getRecentRequests(Hospital hospital) {
        return bloodRequestRepository.findByHospitalWithDetailsOrderByRequestedAtDesc(hospital, PageRequest.of(0, 5))
                .getContent();
    }

    public List<BloodDonation> getRecentDonations(Hospital hospital) {
        return bloodDonationRepository.findByHospitalWithDetailsOrderByPreferredDateAsc(hospital, PageRequest.of(0, 5))
                .getContent();
    }

    public List<BloodInventory> getLowStockAlerts(Hospital hospital) {
        return bloodInventoryRepository.findByHospitalAndUnitsAvailableLessThanOrderByUnitsAvailableAsc(hospital, 5);
    }

    public long getTotalHospitals() {
        return hospitalRepository.count();
    }

    public long getTotalRegisteredUsers() {
        return userRepository.countByRole(com.redhope.enums.Role.ROLE_USER);
    }

    public long getTotalBloodDonations() {
        return bloodDonationRepository.count();
    }

    public long getTotalBloodRequests() {
        return bloodRequestRepository.count();
    }

    public long getFulfilledRequests() {
        return bloodRequestRepository.countByStatus(RequestStatus.COMPLETED);
    }

    public long getPendingRequestsCount() {
        return bloodRequestRepository.countByStatus(RequestStatus.PENDING);
    }
}
