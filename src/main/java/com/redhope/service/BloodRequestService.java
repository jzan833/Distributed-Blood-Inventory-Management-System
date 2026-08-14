package com.redhope.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redhope.dto.request.BloodRequestDTO;
import com.redhope.entity.BloodInventory;
import com.redhope.entity.BloodRequest;
import com.redhope.entity.Hospital;
import com.redhope.entity.User;
import com.redhope.enums.BloodType;
import com.redhope.enums.HospitalStatus;
import com.redhope.enums.RequestStatus;
import com.redhope.enums.Urgency;
import com.redhope.event.CriticalRequestEvent;
import com.redhope.repository.BloodInventoryRepository;
import com.redhope.repository.BloodRequestRepository;
import com.redhope.repository.HospitalRepository;

@Service
@Transactional
public class BloodRequestService {

    private static final Logger logger = LoggerFactory.getLogger(BloodRequestService.class);

    private final BloodRequestRepository bloodRequestRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodInventoryRepository bloodInventoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    public BloodRequestService(BloodRequestRepository bloodRequestRepository,
                               HospitalRepository hospitalRepository,
                               BloodInventoryRepository bloodInventoryRepository,
                               ApplicationEventPublisher eventPublisher,
                               NotificationService notificationService) {
        this.bloodRequestRepository = bloodRequestRepository;
        this.hospitalRepository = hospitalRepository;
        this.bloodInventoryRepository = bloodInventoryRepository;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
    }

    public BloodRequest createBloodRequest(BloodRequestDTO dto, User user) {
        Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                .orElseThrow(() -> new IllegalArgumentException("Selected hospital not found"));

        BloodType bloodType = BloodType.valueOf(dto.getBloodType());
        Urgency urgency = Urgency.valueOf(dto.getUrgency());

        BloodRequest request = new BloodRequest();
        request.setRequester(user);
        request.setHospital(hospital);
        request.setBloodType(bloodType);
        request.setUrgency(urgency);
        request.setStatus(RequestStatus.PENDING);
        request.setMedicalReason(dto.getMedicalReason());
        request.setDoctorReferralNumber(dto.getDoctorReferralNumber());

        BloodRequest savedRequest = bloodRequestRepository.save(request);

        if (urgency == Urgency.CRITICAL) {
            BloodInventory inventory = bloodInventoryRepository
                    .findByHospitalAndBloodType(hospital, bloodType)
                    .orElse(null);
            if (inventory != null && inventory.getUnitsAvailable() == 0) {
                logger.info("Triggering critical broadcast for request #{}", savedRequest.getId());
                eventPublisher.publishEvent(new CriticalRequestEvent(savedRequest));
            }
        }

        return savedRequest;
    }

    @Transactional(readOnly = true)
    public List<Hospital> getActiveHospitals() {
        return hospitalRepository.findByStatusOrderByNameAsc(HospitalStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<BloodRequest> getUserRequests(User user) {
        return bloodRequestRepository.findByRequesterWithHospitalOrderByRequestedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<BloodRequest> getRequestsForHospital(Hospital hospital) {
        return bloodRequestRepository.findByHospitalWithDetailsOrderByRequestedAtDesc(hospital, org.springframework.data.domain.PageRequest.of(0, 100))
                .getContent();
    }

    @Transactional(readOnly = true)
    public List<BloodRequest> getRequestsForHospital(Hospital hospital, RequestStatus status) {
        if (status == null) {
            return getRequestsForHospital(hospital);
        }
        return bloodRequestRepository.findByHospitalAndStatusWithDetailsOrderByRequestedAtDesc(hospital, status);
    }

    public BloodRequest approveRequest(Long requestId, Hospital hospital) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Blood request not found with id: " + requestId));

        if (!request.getHospital().getId().equals(hospital.getId())) {
            throw new IllegalArgumentException("You are not authorized to approve this request. It does not belong to your hospital.");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be approved. Current status: " + request.getStatus().getDisplayName());
        }

        request.setStatus(RequestStatus.APPROVED);
        BloodRequest saved = bloodRequestRepository.save(request);

        logger.info("Request #{} approved by hospital={}", requestId, hospital.getName());

        notificationService.notifyRequesterOfStatusChange(saved);

        return saved;
    }

    public BloodRequest rejectRequest(Long requestId, Hospital hospital, String rejectionReason) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Blood request not found with id: " + requestId));

        if (!request.getHospital().getId().equals(hospital.getId())) {
            throw new IllegalArgumentException("You are not authorized to reject this request. It does not belong to your hospital.");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be rejected. Current status: " + request.getStatus().getDisplayName());
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(rejectionReason);
        BloodRequest saved = bloodRequestRepository.save(request);

        logger.info("Request #{} rejected by hospital={}", requestId, hospital.getName());

        notificationService.notifyRequesterOfStatusChange(saved);

        return saved;
    }
}
