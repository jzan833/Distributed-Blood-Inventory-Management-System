package com.redhope.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redhope.dto.request.DonationDTO;
import com.redhope.entity.BloodDonation;
import com.redhope.entity.BloodInventory;
import com.redhope.entity.Hospital;
import com.redhope.entity.User;
import com.redhope.enums.BloodType;
import com.redhope.enums.DonationStatus;
import com.redhope.repository.BloodDonationRepository;
import com.redhope.repository.BloodInventoryRepository;
import com.redhope.repository.HospitalRepository;
import com.redhope.repository.UserRepository;
import com.redhope.service.NotificationService;

@Service
@Transactional
public class DonationService {

    private static final Logger logger = LoggerFactory.getLogger(DonationService.class);

    private final BloodDonationRepository bloodDonationRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final BloodInventoryRepository bloodInventoryRepository;
    private final NotificationService notificationService;

    public DonationService(BloodDonationRepository bloodDonationRepository,
                           HospitalRepository hospitalRepository,
                           UserRepository userRepository,
                           BloodInventoryRepository bloodInventoryRepository,
                           NotificationService notificationService) {
        this.bloodDonationRepository = bloodDonationRepository;
        this.hospitalRepository = hospitalRepository;
        this.userRepository = userRepository;
        this.bloodInventoryRepository = bloodInventoryRepository;
        this.notificationService = notificationService;
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

    @Transactional(readOnly = true)
    public List<BloodDonation> getUserDonations(User user) {
        return bloodDonationRepository.findByDonorWithHospitalOrderByRequestedAtDesc(user);
    }

    public BloodDonation createDonation(DonationDTO dto, User user) {
        if (!isEligibleToDonate(user)) {
            throw new IllegalArgumentException(
                    "You are not eligible to donate yet. Please wait " + getDaysUntilEligible(user) + " more days.");
        }

        Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                .orElseThrow(() -> new IllegalArgumentException("Selected hospital not found"));

        BloodDonation donation = new BloodDonation();
        donation.setDonor(user);
        donation.setHospital(hospital);
        donation.setPreferredDate(dto.getPreferredDate());
        donation.setStatus(DonationStatus.PENDING);
        donation.setHealthChecklistPassed(true);

        BloodDonation savedDonation = bloodDonationRepository.save(donation);

        return savedDonation;
    }

    public BloodDonation approveDonation(Long donationId, Hospital hospital, User actor) {
        BloodDonation donation = bloodDonationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Blood donation not found with id: " + donationId));

        if (!donation.getHospital().getId().equals(hospital.getId())) {
            throw new IllegalArgumentException("You are not authorized to approve this donation. It does not belong to your hospital.");
        }

        if (donation.getStatus() != DonationStatus.PENDING) {
            throw new IllegalArgumentException("Only pending donations can be approved. Current status: " + donation.getStatus().getDisplayName());
        }

        DonationStatus oldStatus = donation.getStatus();
        donation.setStatus(DonationStatus.APPROVED);
        BloodDonation saved = bloodDonationRepository.save(donation);

        logger.info("Donation #{} approved by hospital={}", donationId, hospital.getName());

        notificationService.notifyDonorOfStatusChange(saved, hospital);

        return saved;
    }

    public BloodDonation completeDonation(Long donationId, Hospital hospital, User actor) {
        BloodDonation donation = bloodDonationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Blood donation not found with id: " + donationId));

        if (!donation.getHospital().getId().equals(hospital.getId())) {
            throw new IllegalArgumentException("You are not authorized to complete this donation. It does not belong to your hospital.");
        }

        if (donation.getStatus() != DonationStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved donations can be marked as completed. Current status: " + donation.getStatus().getDisplayName());
        }

        User donor = donation.getDonor();
        BloodType donorBloodType = donor.getBloodType();

        int updatedRows = bloodInventoryRepository.adjustStock(hospital, donorBloodType, 1);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Failed to update blood inventory for hospital=" + hospital.getName() + ", bloodType=" + donorBloodType);
        }

        donor.setLastDonationDate(LocalDate.now());
        userRepository.save(donor);

        donation.setStatus(DonationStatus.COMPLETED);
        BloodDonation saved = bloodDonationRepository.save(donation);

        logger.info("Donation #{} completed by hospital={}, inventory updated for bloodType={}",
                donationId, hospital.getName(), donorBloodType);
        return saved;
    }

    public BloodDonation cancelDonation(Long donationId, Hospital hospital, User actor, String reason) {
        BloodDonation donation = bloodDonationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Blood donation not found with id: " + donationId));

        if (!donation.getHospital().getId().equals(hospital.getId())) {
            throw new IllegalArgumentException("You are not authorized to cancel this donation. It does not belong to your hospital.");
        }

        if (donation.getStatus() != DonationStatus.PENDING && donation.getStatus() != DonationStatus.APPROVED) {
            throw new IllegalArgumentException("Only pending or approved donations can be cancelled. Current status: " + donation.getStatus().getDisplayName());
        }

        DonationStatus oldStatus = donation.getStatus();
        donation.setStatus(DonationStatus.CANCELLED);
        donation.setRejectionReason(reason != null && !reason.trim().isEmpty() ? reason.trim() : "No reason provided");
        BloodDonation saved = bloodDonationRepository.save(donation);

        logger.info("Donation #{} cancelled by hospital={}", donationId, hospital.getName());

        notificationService.notifyDonorOfStatusChange(saved, hospital);

        return saved;
    }
}
