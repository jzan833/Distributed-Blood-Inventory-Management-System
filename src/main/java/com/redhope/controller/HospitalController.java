package com.redhope.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.redhope.entity.BloodDonation;
import com.redhope.entity.BloodInventory;
import com.redhope.entity.BloodRequest;
import com.redhope.entity.Hospital;
import com.redhope.entity.User;
import com.redhope.enums.BloodType;
import com.redhope.enums.DonationStatus;
import com.redhope.enums.RequestStatus;
import com.redhope.repository.HospitalRepository;
import com.redhope.repository.UserRepository;
import com.redhope.repository.BloodDonationRepository;
import com.redhope.service.DashboardService;
import com.redhope.service.DonationService;
import com.redhope.service.InventoryService;
import com.redhope.service.BloodRequestService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/hospital")
public class HospitalController {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final DashboardService dashboardService;
    private final InventoryService inventoryService;
    private final BloodRequestService bloodRequestService;
    private final BloodDonationRepository bloodDonationRepository;
    private final DonationService donationService;

    public HospitalController(UserRepository userRepository,
                            HospitalRepository hospitalRepository,
                            DashboardService dashboardService,
                            InventoryService inventoryService,
                            BloodRequestService bloodRequestService,
                            BloodDonationRepository bloodDonationRepository,
                            DonationService donationService) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.dashboardService = dashboardService;
        this.inventoryService = inventoryService;
        this.bloodRequestService = bloodRequestService;
        this.bloodDonationRepository = bloodDonationRepository;
        this.donationService = donationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User admin = getCurrentUser();
        Hospital hospital = getHospital(admin);

        long totalRequests = dashboardService.getTotalRequests(hospital);
        long totalDonations = dashboardService.getTotalDonations(hospital);
        long pendingRequests = dashboardService.getPendingRequests(hospital);
        long pendingDonations = dashboardService.getPendingDonations(hospital);
        long lowStockAlerts = dashboardService.getLowStockAlertsCount(hospital);
        long todayAppointments = dashboardService.getTodayAppointmentsCount(hospital);

        List<BloodRequest> recentRequests = dashboardService.getRecentRequests(hospital);
        List<BloodDonation> recentDonations = dashboardService.getRecentDonations(hospital);
        List<BloodInventory> lowStockItems = dashboardService.getLowStockAlerts(hospital);

        model.addAttribute("adminName", admin.getFullName());
        model.addAttribute("hospitalName", hospital.getName());
        model.addAttribute("totalRequests", totalRequests);
        model.addAttribute("totalDonations", totalDonations);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("pendingDonations", pendingDonations);
        model.addAttribute("lowStockAlerts", lowStockAlerts);
        model.addAttribute("todayAppointments", todayAppointments);
        model.addAttribute("recentRequests", recentRequests);
        model.addAttribute("recentDonations", recentDonations);
        model.addAttribute("lowStockItems", lowStockItems);

        return "hospital/dashboard";
    }

    @GetMapping("/inventory")
    public String inventory(Model model) {
        User admin = getCurrentUser();
        Hospital hospital = getHospital(admin);

        List<BloodInventory> inventory = inventoryService.getInventoryForHospital(hospital);

        model.addAttribute("adminName", admin.getFullName());
        model.addAttribute("hospitalName", hospital.getName());
        model.addAttribute("inventory", inventory);
        model.addAttribute("bloodTypes", BloodType.values());

        return "hospital/inventory";
    }

    @PostMapping("/inventory/update")
    @ResponseBody
    public Map<String, Object> updateStock(@RequestParam("bloodType") String bloodType,
                                           @RequestParam("unitsAvailable") int unitsAvailable) {
        User admin = getCurrentUser();
        Hospital hospital = getHospital(admin);
        BloodType type = BloodType.valueOf(bloodType);

        BloodInventory updated = inventoryService.updateStock(hospital, type, unitsAvailable, admin);

        return Map.of(
                "success", true,
                "bloodType", type.name(),
                "unitsAvailable", updated.getUnitsAvailable(),
                "lastUpdated", updated.getLastUpdated().toString()
        );
    }

    @PostMapping("/inventory/threshold")
    @ResponseBody
    public Map<String, Object> updateThreshold(@RequestParam("bloodType") String bloodType,
                                               @RequestParam("lowStockThreshold") int lowStockThreshold) {
        User admin = getCurrentUser();
        Hospital hospital = getHospital(admin);
        BloodType type = BloodType.valueOf(bloodType);

        BloodInventory updated = inventoryService.updateThreshold(hospital, type, lowStockThreshold, admin);

        return Map.of(
                "success", true,
                "bloodType", type.name(),
                "lowStockThreshold", updated.getLowStockThreshold(),
                "lastUpdated", updated.getLastUpdated().toString()
        );
    }

    @GetMapping("/requests")
    public String requests(Model model, @RequestParam(required = false) String status) {
        User admin = getCurrentUser();
        Hospital hospital = getHospital(admin);

        List<BloodRequest> requests;
        if (status != null && !status.isEmpty()) {
            try {
                RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
                requests = bloodRequestService.getRequestsForHospital(hospital, requestStatus);
            } catch (IllegalArgumentException e) {
                requests = bloodRequestService.getRequestsForHospital(hospital);
            }
        } else {
            requests = bloodRequestService.getRequestsForHospital(hospital);
        }

        model.addAttribute("adminName", admin.getFullName());
        model.addAttribute("hospitalName", hospital.getName());
        model.addAttribute("requests", requests);
        model.addAttribute("currentFilter", status != null ? status.toUpperCase() : "ALL");
        model.addAttribute("requestStatuses", RequestStatus.values());

        long pendingCount = bloodRequestService.getRequestsForHospital(hospital, RequestStatus.PENDING).size();
        long approvedCount = bloodRequestService.getRequestsForHospital(hospital, RequestStatus.APPROVED).size();
        long rejectedCount = bloodRequestService.getRequestsForHospital(hospital, RequestStatus.REJECTED).size();
        long completedCount = bloodRequestService.getRequestsForHospital(hospital, RequestStatus.COMPLETED).size();

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("completedCount", completedCount);

        return "hospital/requests";
    }

    @PostMapping("/requests/{id}/approve")
    public String approveRequest(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            User admin = getCurrentUser();
            Hospital hospital = getHospital(admin);

            BloodRequest updated = bloodRequestService.approveRequest(id, hospital);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Request #" + updated.getId() + " has been approved successfully.");

            return "redirect:/hospital/requests";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/hospital/requests";
        }
    }

    @PostMapping("/requests/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam(required = false) String rejectionReason,
                                Model model,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {
        try {
            User admin = getCurrentUser();
            Hospital hospital = getHospital(admin);

            String reason = (rejectionReason != null && !rejectionReason.trim().isEmpty())
                    ? rejectionReason.trim()
                    : "No reason provided";

            BloodRequest updated = bloodRequestService.rejectRequest(id, hospital, reason);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Request #" + updated.getId() + " has been rejected.");

            return "redirect:/hospital/requests";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/hospital/requests";
        }
    }

    @GetMapping("/donations")
    public String donations(Model model, @RequestParam(required = false) String status) {
        User admin = getCurrentUser();
        Hospital hospital = getHospital(admin);

        List<BloodDonation> donations;
        if (status != null && !status.isEmpty()) {
            try {
                DonationStatus donationStatus = DonationStatus.valueOf(status.toUpperCase());
                donations = bloodDonationRepository.findByHospitalAndStatusWithDetailsOrderByPreferredDateAsc(hospital, donationStatus);
            } catch (IllegalArgumentException e) {
                donations = bloodDonationRepository.findByHospitalWithDetailsListOrderByPreferredDateAsc(hospital);
            }
        } else {
            donations = bloodDonationRepository.findByHospitalWithDetailsListOrderByPreferredDateAsc(hospital);
        }

        model.addAttribute("adminName", admin.getFullName());
        model.addAttribute("hospitalName", hospital.getName());
        model.addAttribute("donations", donations);
        model.addAttribute("currentFilter", status != null ? status.toUpperCase() : "ALL");
        model.addAttribute("donationStatuses", DonationStatus.values());

        long pendingCount = bloodDonationRepository.countByHospitalAndStatus(hospital, DonationStatus.PENDING);
        long approvedCount = bloodDonationRepository.countByHospitalAndStatus(hospital, DonationStatus.APPROVED);
        long completedCount = bloodDonationRepository.countByHospitalAndStatus(hospital, DonationStatus.COMPLETED);
        long cancelledCount = bloodDonationRepository.countByHospitalAndStatus(hospital, DonationStatus.CANCELLED);

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);

        return "hospital/donations";
    }

    @PostMapping("/donations/{id}/approve")
    public String approveDonation(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentUser();
            Hospital hospital = getHospital(admin);

            BloodDonation updated = donationService.approveDonation(id, hospital, admin);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Donation #" + updated.getId() + " has been approved successfully.");

            return "redirect:/hospital/donations";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/hospital/donations";
        }
    }

    @PostMapping("/donations/{id}/complete")
    public String completeDonation(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentUser();
            Hospital hospital = getHospital(admin);

            BloodDonation updated = donationService.completeDonation(id, hospital, admin);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Donation #" + updated.getId() + " has been marked as completed.");

            return "redirect:/hospital/donations";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/hospital/donations";
        }
    }

    @PostMapping("/donations/{id}/cancel")
    public String cancelDonation(@PathVariable Long id,
                                 @RequestParam(required = false) String reason,
                                 RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentUser();
            Hospital hospital = getHospital(admin);

            String cancelReason = (reason != null && !reason.trim().isEmpty()) ? reason.trim() : "No reason provided";

            BloodDonation updated = donationService.cancelDonation(id, hospital, admin, cancelReason);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Donation #" + updated.getId() + " has been cancelled.");

            return "redirect:/hospital/donations";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/hospital/donations";
        }
    }

    private void addCommonAttributes(Model model) {
        User admin = getCurrentUser();
        Hospital hospital = getHospital(admin);
        model.addAttribute("adminName", admin.getFullName());
        model.addAttribute("hospitalName", hospital.getName());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }

    private Hospital getHospital(User admin) {
        if (admin.getHospitalId() == null) {
            throw new IllegalStateException("Hospital admin is not assigned to a hospital: " + admin.getEmail());
        }
        return hospitalRepository.findById(admin.getHospitalId())
                .orElseThrow(() -> new IllegalStateException("Hospital not found for admin: " + admin.getEmail()));
    }
}
