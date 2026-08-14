package com.redhope.repository;

import com.redhope.entity.BloodDonation;
import com.redhope.entity.Hospital;
import com.redhope.entity.User;
import com.redhope.enums.DonationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BloodDonationRepository extends JpaRepository<BloodDonation, Long> {

    List<BloodDonation> findByDonorOrderByRequestedAtDesc(User donor);

    Page<BloodDonation> findByHospitalOrderByPreferredDateAsc(Hospital hospital, Pageable pageable);

    @Query("SELECT bd FROM BloodDonation bd JOIN FETCH bd.donor JOIN FETCH bd.hospital WHERE bd.hospital = :hospital ORDER BY bd.preferredDate ASC")
    Page<BloodDonation> findByHospitalWithDetailsOrderByPreferredDateAsc(@Param("hospital") Hospital hospital, Pageable pageable);

    List<BloodDonation> findByHospitalAndStatusOrderByPreferredDateAsc(Hospital hospital, DonationStatus status);

    List<BloodDonation> findByHospitalAndPreferredDateBetweenOrderByPreferredDateAsc(Hospital hospital, LocalDate start, LocalDate end);

    List<BloodDonation> findByHospitalOrderByPreferredDateAsc(Hospital hospital);

    @Query("SELECT bd FROM BloodDonation bd JOIN FETCH bd.donor JOIN FETCH bd.hospital WHERE bd.hospital = :hospital ORDER BY bd.preferredDate ASC")
    List<BloodDonation> findByHospitalWithDetailsListOrderByPreferredDateAsc(@Param("hospital") Hospital hospital);

    @Query("SELECT bd FROM BloodDonation bd JOIN FETCH bd.donor JOIN FETCH bd.hospital WHERE bd.hospital = :hospital AND bd.status = :status ORDER BY bd.preferredDate ASC")
    List<BloodDonation> findByHospitalAndStatusWithDetailsOrderByPreferredDateAsc(@Param("hospital") Hospital hospital, @Param("status") DonationStatus status);

    List<BloodDonation> findByDonorAndStatusInOrderByRequestedAtDesc(User donor, List<DonationStatus> statuses);

    long countByHospital(Hospital hospital);

    long countByHospitalAndStatus(Hospital hospital, DonationStatus status);

    @Query("SELECT bd FROM BloodDonation bd JOIN FETCH bd.hospital JOIN FETCH bd.donor WHERE bd.donor = :donor ORDER BY bd.requestedAt DESC")
    List<BloodDonation> findByDonorWithHospitalOrderByRequestedAtDesc(@Param("donor") User donor);

    @Query("SELECT bd FROM BloodDonation bd WHERE bd.hospital = :hospital AND bd.status = :status AND bd.preferredDate = :date")
    List<BloodDonation> findAppointmentsByHospitalAndStatusAndDate(
            @Param("hospital") Hospital hospital,
            @Param("status") DonationStatus status,
            @Param("date") LocalDate date);

    @Query("SELECT bd FROM BloodDonation bd JOIN FETCH bd.hospital JOIN FETCH bd.donor ORDER BY bd.requestedAt DESC")
    List<BloodDonation> findTop10ByOrderByRequestedAtDesc();
}
