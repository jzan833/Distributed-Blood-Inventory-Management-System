package com.redhope.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redhope.entity.Hospital;
import com.redhope.entity.User;
import com.redhope.enums.BloodType;
import com.redhope.enums.HospitalStatus;
import com.redhope.enums.Role;
import com.redhope.enums.UserStatus;
import com.redhope.repository.HospitalRepository;
import com.redhope.repository.UserRepository;

@Service
public class HospitalService {

    private static final Logger logger = LoggerFactory.getLogger(HospitalService.class);

    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public HospitalService(HospitalRepository hospitalRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.hospitalRepository = hospitalRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAllByOrderByCreatedAtDesc(org.springframework.data.domain.PageRequest.of(0, 100));
    }

    public Hospital getHospitalById(Long id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found with id: " + id));
    }

    @Transactional
    public Hospital createHospital(String name, String city, String address, String contactEmail, String contactPhone,
                                    String adminUsername, String adminEmail, String tempPassword,
                                    User actor) {
        if (hospitalRepository.findByNameAndCity(name, city).isPresent()) {
            throw new IllegalArgumentException("A hospital with this name already exists in " + city);
        }

        Hospital hospital = new Hospital();
        hospital.setName(name);
        hospital.setCity(city);
        hospital.setAddress(address);
        hospital.setContactEmail(contactEmail);
        hospital.setContactPhone(contactPhone);
        hospital.setStatus(HospitalStatus.ACTIVE);
        hospital = hospitalRepository.save(hospital);

        if (adminEmail != null && !adminEmail.trim().isEmpty()) {
            if (userRepository.existsByEmail(adminEmail)) {
                throw new IllegalArgumentException("Email " + adminEmail + " is already registered.");
            }

            User admin = new User();
            admin.setFullName(adminUsername != null && !adminUsername.trim().isEmpty() ? adminUsername : "Hospital Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(tempPassword != null && !tempPassword.isEmpty() ? tempPassword : "admin123"));
            admin.setPhone(contactPhone);
            admin.setBloodType(BloodType.O_POSITIVE);
            admin.setCity(city);
            admin.setRole(Role.ROLE_HOSPITAL_ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setHospitalId(hospital.getId());
            userRepository.save(admin);
        }

        logger.info("Hospital created: id={}, name={}, city={}", hospital.getId(), hospital.getName(), hospital.getCity());
        return hospital;
    }

    @Transactional
    public Hospital updateHospital(Long hospitalId, String name, String city, String address,
                                    String contactEmail, String contactPhone,
                                    User actor) {
        Hospital hospital = getHospitalById(hospitalId);

        if (name != null && !name.trim().isEmpty()) {
            hospital.setName(name.trim());
        }
        if (city != null && !city.trim().isEmpty()) {
            hospital.setCity(city.trim());
        }
        if (address != null && !address.trim().isEmpty()) {
            hospital.setAddress(address.trim());
        }
        if (contactEmail != null && !contactEmail.trim().isEmpty()) {
            hospital.setContactEmail(contactEmail.trim());
        }
        if (contactPhone != null && !contactPhone.trim().isEmpty()) {
            hospital.setContactPhone(contactPhone.trim());
        }

        hospital = hospitalRepository.save(hospital);

        logger.info("Hospital updated: id={}, name={}", hospital.getId(), hospital.getName());
        return hospital;
    }

    @Transactional
    public Hospital toggleHospitalStatus(Long hospitalId, User actor) {
        Hospital hospital = getHospitalById(hospitalId);

        if (hospital.getStatus() == HospitalStatus.ACTIVE) {
            hospital.setStatus(HospitalStatus.SUSPENDED);
        } else if (hospital.getStatus() == HospitalStatus.SUSPENDED) {
            hospital.setStatus(HospitalStatus.ACTIVE);
        } else {
            throw new IllegalArgumentException("Only ACTIVE and SUSPENDED hospitals can be toggled.");
        }

        hospital = hospitalRepository.save(hospital);

        logger.info("Hospital status toggled: id={}, newStatus={}", hospital.getId(), hospital.getStatus());
        return hospital;
    }

    public List<User> getHospitalAdmins(Long hospitalId) {
        return userRepository.findByHospitalIdAndRole(hospitalId, Role.ROLE_HOSPITAL_ADMIN);
    }

    public Map<Long, String> getHospitalAdminNames(List<Hospital> hospitals) {
        return hospitals.stream()
                .collect(Collectors.toMap(
                        Hospital::getId,
                        h -> {
                            List<User> admins = userRepository.findByHospitalIdAndRole(h.getId(), Role.ROLE_HOSPITAL_ADMIN);
                            return admins.isEmpty() ? "Unassigned" : admins.get(0).getFullName();
                        }
                ));
    }

    public Map<Long, String> getHospitalAdminEmails(List<Hospital> hospitals) {
        return hospitals.stream()
                .collect(Collectors.toMap(
                        Hospital::getId,
                        h -> {
                            List<User> admins = userRepository.findByHospitalIdAndRole(h.getId(), Role.ROLE_HOSPITAL_ADMIN);
                            return admins.isEmpty() ? "N/A" : admins.get(0).getEmail();
                        }
                ));
    }
}
