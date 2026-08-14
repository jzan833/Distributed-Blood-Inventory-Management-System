package com.redhope.controller;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.redhope.entity.BloodDonation;
import com.redhope.entity.BloodRequest;
import com.redhope.entity.Hospital;
import com.redhope.entity.User;
import com.redhope.enums.BloodType;
import com.redhope.enums.HospitalStatus;
import com.redhope.enums.RequestStatus;
import com.redhope.enums.Role;
import com.redhope.enums.Urgency;
import com.redhope.enums.UserStatus;
import com.redhope.repository.BloodDonationRepository;
import com.redhope.repository.BloodRequestRepository;
import com.redhope.repository.HospitalRepository;
import com.redhope.repository.UserRepository;
import com.redhope.service.DashboardService;
import com.redhope.service.HospitalService;
import com.redhope.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final BloodDonationRepository bloodDonationRepository;
    private final DashboardService dashboardService;
    private final HospitalService hospitalService;
    private final UserService userService;

    public AdminController(UserRepository userRepository,
                           HospitalRepository hospitalRepository,
                           BloodRequestRepository bloodRequestRepository,
                           BloodDonationRepository bloodDonationRepository,
                           DashboardService dashboardService,
                           HospitalService hospitalService,
                           UserService userService) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.bloodDonationRepository = bloodDonationRepository;
        this.dashboardService = dashboardService;
        this.hospitalService = hospitalService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User admin = getCurrentUser();

        long totalHospitals = dashboardService.getTotalHospitals();
        long totalUsers = dashboardService.getTotalRegisteredUsers();
        long totalDonations = dashboardService.getTotalBloodDonations();
        long totalRequests = dashboardService.getTotalBloodRequests();
        long fulfilledRequests = dashboardService.getFulfilledRequests();
        long pendingRequests = dashboardService.getPendingRequestsCount();

        model.addAttribute("adminName", admin.getFullName());
        model.addAttribute("totalHospitals", totalHospitals);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalDonations", totalDonations);
        model.addAttribute("totalRequests", totalRequests);
        model.addAttribute("fulfilledRequests", fulfilledRequests);
        model.addAttribute("pendingRequests", pendingRequests);

        Pageable pageable = PageRequest.of(0, 5);

        List<User> recentUsers = userRepository.findByRole(Role.ROLE_USER, pageable).getContent();
        List<Hospital> recentHospitals = hospitalRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<BloodRequest> recentCriticalRequests = bloodRequestRepository.findTop10ByOrderByRequestedAtDesc().stream()
                .filter(r -> r.getUrgency() == Urgency.CRITICAL)
                .limit(5)
                .collect(Collectors.toList());

        List<Map<String, Object>> activities = new ArrayList<>();

        for (User u : recentUsers) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "user_signup");
            activity.put("title", "New User Registered");
            activity.put("description", u.getFullName() + " (" + u.getEmail() + ")");
            activity.put("date", u.getCreatedAt());
            activity.put("icon", "fas fa-user-plus");
            activity.put("color", "text-success");
            activities.add(activity);
        }

        for (Hospital h : recentHospitals) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "hospital_registered");
            activity.put("title", "New Hospital Registered");
            activity.put("description", h.getName() + " in " + h.getCity());
            activity.put("date", h.getCreatedAt());
            activity.put("icon", "fas fa-hospital");
            activity.put("color", "text-primary");
            activities.add(activity);
        }

        for (BloodRequest br : recentCriticalRequests) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "critical_request");
            activity.put("title", "Critical Blood Request");
            activity.put("description", br.getBloodType().getDisplayName() + " requested at " + br.getHospital().getName());
            activity.put("date", br.getRequestedAt());
            activity.put("icon", "fas fa-exclamation-circle");
            activity.put("color", "text-danger");
            activities.add(activity);
        }

        activities.sort((a, b) -> {
            LocalDateTime dateA = (LocalDateTime) a.get("date");
            LocalDateTime dateB = (LocalDateTime) b.get("date");
            return dateB.compareTo(dateA);
        });

        model.addAttribute("recentActivities", activities.stream().limit(10).collect(Collectors.toList()));

        return "admin/dashboard";
    }

    @GetMapping("/hospitals")
    public String hospitals(Model model,
                            @RequestParam(required = false) String city,
                            @RequestParam(required = false) String status) {
        User admin = getCurrentUser();

        List<Hospital> hospitals = hospitalService.getAllHospitals();

        if (city != null && !city.trim().isEmpty()) {
            String cityFilter = city.trim().toLowerCase();
            hospitals = hospitals.stream()
                    .filter(h -> h.getCity() != null && h.getCity().toLowerCase().contains(cityFilter))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty()) {
            try {
                HospitalStatus hospitalStatus = HospitalStatus.valueOf(status.trim().toUpperCase());
                hospitals = hospitals.stream()
                        .filter(h -> h.getStatus() == hospitalStatus)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // ignore invalid status filter
            }
        }

        Map<Long, String> adminNames = hospitalService.getHospitalAdminNames(hospitals);
        Map<Long, String> adminEmails = hospitalService.getHospitalAdminEmails(hospitals);

        model.addAttribute("adminName", admin.getFullName());
        model.addAttribute("hospitals", hospitals);
        model.addAttribute("adminNames", adminNames);
        model.addAttribute("adminEmails", adminEmails);
        model.addAttribute("hospitalStatuses", HospitalStatus.values());
        model.addAttribute("currentCityFilter", city != null ? city : "");
        model.addAttribute("currentStatusFilter", status != null ? status.toUpperCase() : "");

        return "admin/hospitals";
    }

    @PostMapping("/hospitals")
    public String createHospital(@RequestParam String name,
                                 @RequestParam String city,
                                 @RequestParam String address,
                                 @RequestParam String contactEmail,
                                 @RequestParam String contactPhone,
                                 @RequestParam(required = false) String adminUsername,
                                 @RequestParam(required = false) String adminEmail,
                                 @RequestParam(required = false) String tempPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentUser();
            hospitalService.createHospital(name, city, address, contactEmail, contactPhone,
                    adminUsername, adminEmail, tempPassword, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Hospital registered successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/hospitals";
    }

    @PostMapping("/hospitals/{id}/edit")
    public String updateHospital(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam String city,
                                 @RequestParam String address,
                                 @RequestParam String contactEmail,
                                 @RequestParam String contactPhone,
                                 RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentUser();
            hospitalService.updateHospital(id, name, city, address, contactEmail, contactPhone, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Hospital updated successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/hospitals";
    }

    @PostMapping("/hospitals/{id}/suspend")
    public String suspendHospital(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentUser();
            Hospital hospital = hospitalService.toggleHospitalStatus(id, admin);
            String message = hospital.getStatus() == HospitalStatus.SUSPENDED
                    ? "Hospital suspended successfully."
                    : "Hospital activated successfully.";
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/hospitals";
    }

    @GetMapping("/users")
    public String users(Model model,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String email,
                        @RequestParam(required = false) BloodType bloodType,
                        @RequestParam(required = false) UserStatus status) {
        User admin = getCurrentUser();

        List<User> users = userService.searchUsers(name, email, bloodType, status);

        model.addAttribute("adminName", admin.getFullName());
        model.addAttribute("users", users);
        model.addAttribute("bloodTypes", BloodType.values());
        model.addAttribute("userStatuses", UserStatus.values());
        model.addAttribute("currentNameFilter", name != null ? name : "");
        model.addAttribute("currentEmailFilter", email != null ? email : "");
        model.addAttribute("currentBloodTypeFilter", bloodType != null ? bloodType.name() : "");
        model.addAttribute("currentStatusFilter", status != null ? status.name() : "");

        return "admin/users";
    }

    @PostMapping("/users/{id}/ban")
    public String banUser(@PathVariable Long id,
                          RedirectAttributes redirectAttributes,
                          HttpServletRequest request) {
        try {
            User admin = getCurrentUser();
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

            if (user.getStatus() == UserStatus.BANNED) {
                userService.unbanUser(id);
                redirectAttributes.addFlashAttribute("successMessage", "User unbanned successfully.");
            } else {
                userService.banUser(id);
                redirectAttributes.addFlashAttribute("successMessage", "User banned successfully.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }
}
