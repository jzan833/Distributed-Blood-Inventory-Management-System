package com.redhope.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.redhope.dto.EligibilityDTO;
import com.redhope.dto.RecentActivityDTO;
import com.redhope.dto.StepperStatusDTO;
import com.redhope.dto.UrgentNeedDTO;
import com.redhope.dto.request.BloodRequestDTO;
import com.redhope.dto.request.DonationDTO;
import com.redhope.entity.BloodRequest;
import com.redhope.entity.Hospital;
import com.redhope.entity.User;
import com.redhope.enums.BloodType;
import com.redhope.repository.UserRepository;
import com.redhope.service.BloodRequestService;   
import com.redhope.service.DashboardService;
import com.redhope.service.DonationService;
import com.redhope.entity.BloodDonation;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final DashboardService dashboardService;
    private final BloodRequestService bloodRequestService;
    private final DonationService donationService;

    public UserController(UserRepository userRepository,
                          DashboardService dashboardService,
                          BloodRequestService bloodRequestService,
                          DonationService donationService) {
        this.userRepository = userRepository;
        this.dashboardService = dashboardService;
        this.bloodRequestService = bloodRequestService;
        this.donationService = donationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = getCurrentUser();

        EligibilityDTO eligibility = dashboardService.getEligibility(user);
        List<UrgentNeedDTO> urgentNeeds = dashboardService.getUrgentNeeds(user);
        List<RecentActivityDTO> recentActivity = dashboardService.getRecentActivity(user);
        StepperStatusDTO stepper = dashboardService.getStatusStepperData(user);

        model.addAttribute("userName", user.getFullName());
        model.addAttribute("bloodType", user.getBloodType().getDisplayName());
        model.addAttribute("bloodTypeClass", getBloodTypeCssClass(user.getBloodType()));
        model.addAttribute("eligibility", eligibility);
        model.addAttribute("urgentNeeds", urgentNeeds);
        model.addAttribute("recentActivity", recentActivity);
        model.addAttribute("stepper", stepper);

        return "user/dashboard";
    }

    @GetMapping("/request-blood")
    public String requestBloodForm(Model model) {
        try {
            User user = getCurrentUser();
            List<Hospital> hospitals = bloodRequestService.getActiveHospitals();
            BloodRequestDTO dto = new BloodRequestDTO();
            dto.setBloodType(user.getBloodType().name());

            model.addAttribute("bloodRequestDTO", dto);
            model.addAttribute("hospitals", hospitals);
            model.addAttribute("bloodTypes", BloodType.values());
            model.addAttribute("urgencies", com.redhope.enums.Urgency.values());
            model.addAttribute("userBloodType", user.getBloodType().getDisplayName());
            model.addAttribute("userName", user.getFullName());
            return "user/request-blood";
        } catch (Exception e) {
            System.err.println("ERROR in requestBloodForm: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/requests")
    public String submitBloodRequest(@Valid @ModelAttribute("bloodRequestDTO") BloodRequestDTO bloodRequestDTO,
                                     BindingResult bindingResult,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            User user = getCurrentUser();
            model.addAttribute("hospitals", bloodRequestService.getActiveHospitals());
            model.addAttribute("bloodTypes", BloodType.values());
            model.addAttribute("urgencies", com.redhope.enums.Urgency.values());
            model.addAttribute("userBloodType", user.getBloodType().getDisplayName());
            model.addAttribute("userName", user.getFullName());
            return "user/request-blood";
        }

        User user = getCurrentUser();

        try {
            BloodRequest savedRequest = bloodRequestService.createBloodRequest(bloodRequestDTO, user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Your blood request has been submitted successfully! Status: PENDING - A hospital administrator will review your request.");

            if (savedRequest.getUrgency() == com.redhope.enums.Urgency.CRITICAL) {
                redirectAttributes.addFlashAttribute("broadcastMessage",
                        "This is a CRITICAL request with zero stock at the selected hospital. " +
                        "An urgent broadcast has been triggered to all eligible donors in your city.");
            }

            return "redirect:/user/history";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("hospitals", bloodRequestService.getActiveHospitals());
            model.addAttribute("bloodTypes", BloodType.values());
            model.addAttribute("urgencies", com.redhope.enums.Urgency.values());
            model.addAttribute("userBloodType", user.getBloodType().getDisplayName());
            model.addAttribute("userName", user.getFullName());
            return "user/request-blood";
        }
    }

    @GetMapping("/donate-blood")
    public String donateBloodForm(Model model) {
        try {
            User user = getCurrentUser();
            EligibilityDTO eligibility = dashboardService.getEligibility(user);
            List<Hospital> hospitals = bloodRequestService.getActiveHospitals();
            DonationDTO dto = new DonationDTO();

            model.addAttribute("userName", user.getFullName());
            model.addAttribute("eligibility", eligibility);
            model.addAttribute("hospitals", hospitals);
            model.addAttribute("donationDTO", dto);
            return "user/donate-blood";
        } catch (Exception e) {
            System.err.println("ERROR in donateBloodForm: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/donations")
    public String submitDonation(@Valid @ModelAttribute("donationDTO") DonationDTO donationDTO,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            User user = getCurrentUser();
            model.addAttribute("userName", user.getFullName());
            model.addAttribute("eligibility", dashboardService.getEligibility(user));
            model.addAttribute("hospitals", bloodRequestService.getActiveHospitals());
            return "user/donate-blood";
        }

        User user = getCurrentUser();

        try {
            donationService.createDonation(donationDTO, user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Your donation appointment has been scheduled successfully! Status: PENDING - A hospital administrator will review your appointment.");
            return "redirect:/user/history";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("userName", user.getFullName());
            model.addAttribute("eligibility", dashboardService.getEligibility(user));
            model.addAttribute("hospitals", bloodRequestService.getActiveHospitals());
            return "user/donate-blood";
        }
    }

    @GetMapping("/history")
    public String history(Model model) {
        User user = getCurrentUser();
        List<BloodRequest> requests = bloodRequestService.getUserRequests(user);
        List<BloodDonation> donations = donationService.getUserDonations(user);

        model.addAttribute("userName", user.getFullName());
        model.addAttribute("requests", requests);
        model.addAttribute("donations", donations);
        return "user/history";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }

    private String getBloodTypeCssClass(BloodType bloodType) {
        switch (bloodType) {
            case O_POSITIVE:
            case O_NEGATIVE:
                return "bg-success";
            case A_POSITIVE:
            case A_NEGATIVE:
            case B_POSITIVE:
            case B_NEGATIVE:
                return "bg-warning text-dark";
            case AB_POSITIVE:
            case AB_NEGATIVE:
                return "bg-danger";
            default:
                return "bg-secondary";
        }
    }
}
