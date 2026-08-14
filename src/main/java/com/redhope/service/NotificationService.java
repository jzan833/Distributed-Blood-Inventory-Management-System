package com.redhope.service;

import com.redhope.entity.BloodDonation;
import com.redhope.entity.BloodRequest;
import com.redhope.entity.Hospital;
import com.redhope.entity.User;
import com.redhope.enums.DonationStatus;
import com.redhope.enums.RequestStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final String sender;
    private final EmailTemplateService emailTemplateService;

    public NotificationService(JavaMailSender mailSender,
                               @Value("${spring.mail.from.address}") String sender,
                               EmailTemplateService emailTemplateService) {
        this.mailSender = mailSender;
        this.sender = sender;
        this.emailTemplateService = emailTemplateService;
    }

    @Async("emailTaskExecutor")
    public void notifyRequesterOfStatusChange(BloodRequest request) {
        User requester = request.getRequester();
        if (requester == null || requester.getEmail() == null || requester.getEmail().isEmpty()) {
            logger.warn("Cannot send request status email: requester email is missing for request #{}", request.getId());
            return;
        }

        String subject = "Blood Request " + request.getStatus().getDisplayName() + " - RedHope";
        String plainText = buildRequestStatusEmailBody(request);

        String statusColor = request.getStatus() == RequestStatus.APPROVED ? "#28a745" : "#dc3545";
        String statusText = request.getStatus() == RequestStatus.APPROVED ? "APPROVED" : "REJECTED";
        String message;
        if (request.getStatus() == RequestStatus.APPROVED) {
            message = "Your blood request has been approved. Please come to " + request.getHospital().getName() + " to collect the blood.";
        } else {
            message = "Your blood request has been rejected.";
        }

        List<Map<String, String>> rows = new ArrayList<>();
        rows.add(Map.of("label", "Request ID", "value", "#" + request.getId()));
        rows.add(Map.of("label", "Blood Type", "value", request.getBloodType().getDisplayName()));
        rows.add(Map.of("label", "Hospital", "value", request.getHospital() != null ? request.getHospital().getName() : "N/A"));
        rows.add(Map.of("label", "Hospital City", "value", request.getHospital() != null ? request.getHospital().getCity() : "N/A"));
        rows.add(Map.of("label", "Urgency", "value", request.getUrgency().getDisplayName()));
        rows.add(Map.of("label", "Status", "value", request.getStatus().getDisplayName()));
        rows.add(Map.of("label", "Submitted At", "value", request.getRequestedAt() != null ? request.getRequestedAt().toString() : "N/A"));
        if (request.getStatus() == RequestStatus.REJECTED && request.getRejectionReason() != null && !request.getRejectionReason().isEmpty()) {
            rows.add(Map.of("label", "Rejection Reason", "value", request.getRejectionReason()));
        }

        Map<String, Object> variables = Map.of(
                "recipientName", requester.getFullName(),
                "status", statusText,
                "statusColor", statusColor,
                "message", message,
                "rows", rows,
                "footer", "You can view your request history in the RedHope dashboard."
        );

        String html = emailTemplateService.render("email/request-status", variables);
        sendEmail(requester.getEmail(), subject, plainText, html);
    }

    @Async("emailTaskExecutor")
    public void notifyDonorOfStatusChange(BloodDonation donation, Hospital hospital) {
        User donor = donation.getDonor();
        if (donor == null || donor.getEmail() == null || donor.getEmail().isEmpty()) {
            logger.warn("Cannot send donation status email: donor email is missing for donation #{}", donation.getId());
            return;
        }

        String subject = "Blood Donation " + donation.getStatus().getDisplayName() + " - RedHope";
        String plainText = buildDonationStatusEmailBody(donation, hospital);

        String statusColor;
        String statusText;
        String message;
        if (donation.getStatus() == DonationStatus.APPROVED) {
            statusColor = "#28a745";
            statusText = "APPROVED";
            message = "Your blood donation has been approved. Please come to " + hospital.getName() + " on " + (donation.getPreferredDate() != null ? donation.getPreferredDate().toString() : "the scheduled date") + " to donate blood.";
        } else {
            statusColor = "#dc3545";
            statusText = "CANCELLED";
            message = "Your blood donation appointment at " + hospital.getName() + " has been cancelled.";
        }

        List<Map<String, String>> rows = new ArrayList<>();
        rows.add(Map.of("label", "Donation ID", "value", "#" + donation.getId()));
        rows.add(Map.of("label", "Hospital", "value", hospital.getName()));
        rows.add(Map.of("label", "Hospital City", "value", hospital.getCity()));
        rows.add(Map.of("label", "Preferred Date", "value", donation.getPreferredDate() != null ? donation.getPreferredDate().toString() : "N/A"));
        rows.add(Map.of("label", "Status", "value", donation.getStatus().getDisplayName()));
        if (donation.getStatus() == DonationStatus.CANCELLED && donation.getRejectionReason() != null && !donation.getRejectionReason().isEmpty()) {
            rows.add(Map.of("label", "Cancellation Reason", "value", donation.getRejectionReason()));
        }

        Map<String, Object> variables = Map.of(
                "recipientName", donor.getFullName(),
                "status", statusText,
                "statusColor", statusColor,
                "message", message,
                "rows", rows,
                "footer", "You can view your donation history in the RedHope dashboard."
        );

        String html = emailTemplateService.render("email/donation-status", variables);
        sendEmail(donor.getEmail(), subject, plainText, html);
    }

    @Async("emailTaskExecutor")
    public void notifyMatchingDonorsOfCriticalRequest(BloodRequest request, List<User> matchingDonors) {
        if (matchingDonors == null || matchingDonors.isEmpty()) {
            logger.info("No matching donors found for critical request #{}", request.getId());
            return;
        }

        String subject = "Critical Blood Need: " + request.getBloodType().getDisplayName() + " (" + request.getUrgency().getDisplayName() + ")";

        for (User donor : matchingDonors) {
            if (donor.getEmail() != null && !donor.getEmail().isEmpty()) {
                String plainText = buildCriticalDonorEmailBody(request);

                List<Map<String, String>> rows = new ArrayList<>();
                rows.add(Map.of("label", "Blood Type", "value", request.getBloodType().getDisplayName()));
                rows.add(Map.of("label", "Urgency", "value", request.getUrgency().getDisplayName()));
                rows.add(Map.of("label", "Hospital", "value", request.getHospital() != null ? request.getHospital().getName() : "N/A"));
                rows.add(Map.of("label", "City", "value", request.getHospital() != null ? request.getHospital().getCity() : "N/A"));
                rows.add(Map.of("label", "Request ID", "value", "#" + request.getId()));

                Map<String, Object> variables = Map.of(
                        "recipientName", donor.getFullName(),
                        "message", "There is a critical blood need at " + request.getHospital().getName() + " in " + request.getHospital().getCity() + ". Your blood type (" + request.getBloodType().getDisplayName() + ") is urgently needed.",
                        "rows", rows,
                        "footer", "If you are eligible and willing to donate, please visit the RedHope dashboard or contact the hospital directly."
                );

                String html = emailTemplateService.render("email/critical-donor", variables);
                sendEmail(donor.getEmail(), subject, plainText, html);
            }
        }
    }

    private void sendEmail(String to, String subject, String plainText, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(sender);
            helper.setText(plainText, html);

            mailSender.send(message);
            logger.info("Email sent to '{}' with subject '{}'", to, subject);
        } catch (MessagingException e) {
            logger.error("Failed to send email to '{}' with subject '{}': {}", to, subject, e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to send email to '{}' with subject '{}': {}", to, subject, e.getMessage());
        }
    }

    private String buildRequestStatusEmailBody(BloodRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(request.getRequester().getFullName()).append(",\n\n");

        if (request.getStatus() == RequestStatus.APPROVED) {
            sb.append("Your blood request has been approved. Please come to ").append(request.getHospital().getName()).append(" to collect the blood.\n\n");
        } else {
            sb.append("Your blood request has been rejected.\n\n");
            if (request.getRejectionReason() != null && !request.getRejectionReason().isEmpty()) {
                sb.append("Reason: ").append(request.getRejectionReason()).append("\n\n");
            }
        }

        sb.append("Request Details:\n");
        sb.append("- Request ID: #").append(request.getId()).append("\n");
        sb.append("- Blood Type: ").append(request.getBloodType().getDisplayName()).append("\n");
        sb.append("- Hospital: ").append(request.getHospital() != null ? request.getHospital().getName() : "N/A").append("\n");
        sb.append("- Hospital City: ").append(request.getHospital() != null ? request.getHospital().getCity() : "N/A").append("\n");
        sb.append("- Urgency: ").append(request.getUrgency().getDisplayName()).append("\n");
        sb.append("- Status: ").append(request.getStatus().getDisplayName()).append("\n");
        sb.append("- Submitted At: ").append(request.getRequestedAt()).append("\n");

        sb.append("\nYou can view your request history in the RedHope dashboard.\n\n");
        sb.append("Best regards,\nREDHOPE\nDistributed Blood Inventory");
        return sb.toString();
    }

    private String buildDonationStatusEmailBody(BloodDonation donation, Hospital hospital) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(donation.getDonor().getFullName()).append(",\n\n");

        if (donation.getStatus() == DonationStatus.APPROVED) {
            sb.append("Your blood donation has been approved. Please come to ").append(hospital.getName()).append(" on ").append(donation.getPreferredDate()).append(" to donate blood.\n\n");
        } else {
            sb.append("Your blood donation appointment at ").append(hospital.getName()).append(" has been cancelled.\n\n");
            if (donation.getRejectionReason() != null && !donation.getRejectionReason().isEmpty()) {
                sb.append("Reason: ").append(donation.getRejectionReason()).append("\n\n");
            }
        }

        sb.append("Donation Details:\n");
        sb.append("- Donation ID: #").append(donation.getId()).append("\n");
        sb.append("- Hospital: ").append(hospital.getName()).append("\n");
        sb.append("- Hospital City: ").append(hospital.getCity()).append("\n");
        sb.append("- Preferred Date: ").append(donation.getPreferredDate()).append("\n");
        sb.append("- Status: ").append(donation.getStatus().getDisplayName()).append("\n");

        sb.append("\nYou can view your donation history in the RedHope dashboard.\n\n");
        sb.append("Best regards,\nREDHOPE\nDistributed Blood Inventory");
        return sb.toString();
    }

    private String buildCriticalDonorEmailBody(BloodRequest request) {
        return "Dear Donor,\n\n" +
                "There is a critical blood need at " + request.getHospital().getName() + " in " + request.getHospital().getCity() + ".\n\n" +
                "Critical Need Details:\n" +
                "- Blood Type: " + request.getBloodType().getDisplayName() + "\n" +
                "- Urgency: " + request.getUrgency().getDisplayName() + "\n" +
                "- Hospital: " + request.getHospital().getName() + "\n" +
                "- City: " + request.getHospital().getCity() + "\n\n" +
                "If you are eligible and willing to donate, please visit the RedHope dashboard or contact the hospital directly.\n\n" +
                "Best regards,\nREDHOPE\nDistributed Blood Inventory";
    }
}
