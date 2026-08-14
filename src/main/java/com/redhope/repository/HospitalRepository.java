package com.redhope.repository;

import com.redhope.entity.Hospital;
import com.redhope.enums.HospitalStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    List<Hospital> findByStatusOrderByNameAsc(HospitalStatus status);

    Optional<Hospital> findByNameAndCity(String name, String city);

    List<Hospital> findByCityAndStatus(String city, HospitalStatus status);

    @Query("SELECT h FROM Hospital h ORDER BY h.createdAt DESC")
    List<Hospital> findAllByOrderByCreatedAtDesc(org.springframework.data.domain.Pageable pageable);
}
