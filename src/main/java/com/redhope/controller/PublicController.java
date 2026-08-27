package com.redhope.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.redhope.dto.request.SignupRequest;
import com.redhope.entity.User;
import com.redhope.enums.BloodType;
import com.redhope.service.AuthService;

import jakarta.validation.Valid;

@Controller
@RequestMapping
public class PublicController {

    private final AuthService authService;

    public PublicController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String landing() {
        return "public/landing";
    }

    @GetMapping("/redhope")
    public String redhope() {
        return "public/landing";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {

        // If the user is already logged in,
        // redirect them to the correct dashboard.
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            boolean isSuperAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

            boolean isHospitalAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_HOSPITAL_ADMIN"));

            if (isSuperAdmin) {
                return "redirect:/admin/dashboard";
            } else if (isHospitalAdmin) {
                return "redirect:/hospital/dashboard";
            } else {
                return "redirect:/user/dashboard";
            }
        }

        // Not logged in → show login page
        return "public/login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        SignupRequest signupRequest = new SignupRequest();
        model.addAttribute("signupRequest", signupRequest);
        model.addAttribute("bloodTypes", BloodType.values());
        return "public/signup";
    }

    @PostMapping("/signup")
    public String doSignup(@Valid @ModelAttribute SignupRequest signupRequest,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("bloodTypes", BloodType.values());
            return "public/signup";
        }

        try {
            User user = authService.registerUser(signupRequest);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Registration successful! Please login with your credentials."
            );

            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            bindingResult.reject("signupError", e.getMessage());
            model.addAttribute("bloodTypes", BloodType.values());
            return "public/signup";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}