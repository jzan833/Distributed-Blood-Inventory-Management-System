package com.redhope.repository;

import com.redhope.entity.User;
import com.redhope.enums.BloodType;
import com.redhope.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleOrderByCreatedAtDesc(Role role);

    Page<User> findByRole(Role role, Pageable pageable);

    List<User> findByCityAndRole(String city, Role role);

    List<User> findByBloodTypeAndRole(String bloodType, Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.city = :city AND u.status = 'ACTIVE'")
    List<User> findActiveUsersByCityAndRole(@Param("role") Role role, @Param("city") String city);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.bloodType = :bloodType AND u.status = 'ACTIVE' AND u.city = :city")
    List<User> findActiveDonorsByBloodTypeAndCity(@Param("role") Role role, @Param("bloodType") BloodType bloodType, @Param("city") String city);

    long countByRole(Role role);

    List<User> findByHospitalIdAndRole(Long hospitalId, Role role);
}
