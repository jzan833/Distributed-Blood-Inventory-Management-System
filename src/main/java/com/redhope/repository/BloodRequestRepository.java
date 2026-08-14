package com.redhope.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.redhope.entity.BloodRequest;
import com.redhope.entity.Hospital;
import com.redhope.entity.User;
import com.redhope.enums.BloodType;
import com.redhope.enums.RequestStatus;
import com.redhope.enums.Urgency;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findByRequesterOrderByRequestedAtDesc(User requester);

    Page<BloodRequest> findByRequesterOrderByRequestedAtDesc(User requester, Pageable pageable);

    @Query("SELECT br FROM BloodRequest br JOIN FETCH br.hospital WHERE br.requester = :requester ORDER BY br.requestedAt DESC")
    List<BloodRequest> findByRequesterWithHospitalOrderByRequestedAtDesc(@Param("requester") User requester);

    Page<BloodRequest> findByHospitalOrderByRequestedAtDesc(Hospital hospital, Pageable pageable);

    @Query("SELECT br FROM BloodRequest br JOIN FETCH br.requester JOIN FETCH br.hospital WHERE br.hospital = :hospital ORDER BY br.requestedAt DESC")
    Page<BloodRequest> findByHospitalWithDetailsOrderByRequestedAtDesc(@Param("hospital") Hospital hospital, Pageable pageable);

    List<BloodRequest> findByHospitalAndStatusOrderByRequestedAtDesc(Hospital hospital, RequestStatus status);

    @Query("SELECT br FROM BloodRequest br JOIN FETCH br.requester JOIN FETCH br.hospital WHERE br.hospital = :hospital AND br.status = :status ORDER BY br.requestedAt DESC")
    List<BloodRequest> findByHospitalAndStatusWithDetailsOrderByRequestedAtDesc(@Param("hospital") Hospital hospital, @Param("status") RequestStatus status);

    long countByHospitalAndStatus(Hospital hospital, RequestStatus status);

    long countByHospital(Hospital hospital);

    @Query("SELECT br FROM BloodRequest br WHERE br.status = :status AND br.urgency = 'CRITICAL' AND br.hospital.city = :city")
    List<BloodRequest> findActiveCriticalByCity(@Param("status") RequestStatus status, @Param("city") String city);

    @Query("SELECT br FROM BloodRequest br WHERE br.status = :status AND br.bloodType = :bloodType AND br.hospital = :hospital")
    List<BloodRequest> findByStatusAndBloodTypeAndHospital(@Param("status") RequestStatus status, @Param("bloodType") String bloodType, @Param("hospital") Hospital hospital);

    @Query("SELECT br FROM BloodRequest br JOIN FETCH br.hospital h WHERE br.urgency IN :urgencies AND br.bloodType = :bloodType AND h.city = :city AND br.status IN :statuses ORDER BY br.urgency DESC, br.requestedAt DESC")
    List<BloodRequest> findUrgentNeedsByBloodTypeAndCity(@Param("urgencies") List<Urgency> urgencies, @Param("bloodType") BloodType bloodType, @Param("city") String city, @Param("statuses") List<RequestStatus> statuses);

    long countByStatus(RequestStatus status);

    @Query("SELECT br FROM BloodRequest br JOIN FETCH br.requester JOIN FETCH br.hospital ORDER BY br.requestedAt DESC")
    List<BloodRequest> findTop10ByOrderByRequestedAtDesc();

    @Query("SELECT br FROM BloodRequest br JOIN FETCH br.requester JOIN FETCH br.hospital ORDER BY br.requestedAt DESC")
    Page<BloodRequest> findAllByOrderByRequestedAtDesc(Pageable pageable);
}
