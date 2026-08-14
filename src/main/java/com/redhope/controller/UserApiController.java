package com.redhope.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redhope.dto.EligibilityDTO;
import com.redhope.dto.UrgentNeedDTO;
import com.redhope.entity.User;
import com.redhope.repository.UserRepository;
import com.redhope.service.DashboardService;

@RestController
@RequestMapping("/api/user")
public class UserApiController {

    private final UserRepository userRepository;
    private final DashboardService dashboardService;

    public UserApiController(UserRepository userRepository, DashboardService dashboardService) {
        this.userRepository = userRepository;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/eligibility")
    public EligibilityDTO getEligibility() {
        User user = getCurrentUser();
        return dashboardService.getEligibility(user);
    }

    @GetMapping("/urgent-needs")
    public List<UrgentNeedDTO> getUrgentNeeds() {
        User user = getCurrentUser();
        return dashboardService.getUrgentNeeds(user);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }
}
