package com.redhope.repository;

import com.redhope.entity.BloodInventory;
import com.redhope.entity.Hospital;
import com.redhope.enums.BloodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {

    Optional<BloodInventory> findByHospitalAndBloodType(Hospital hospital, BloodType bloodType);

    List<BloodInventory> findByHospitalOrderByBloodTypeAsc(Hospital hospital);

    List<BloodInventory> findByHospitalAndUnitsAvailableLessThanOrderByUnitsAvailableAsc(Hospital hospital, int threshold);

    long countByHospitalAndUnitsAvailableLessThan(Hospital hospital, int threshold);

    @Modifying
    @Query("UPDATE BloodInventory bi SET bi.unitsAvailable = bi.unitsAvailable + :delta WHERE bi.hospital = :hospital AND bi.bloodType = :bloodType")
    int adjustStock(@Param("hospital") Hospital hospital, @Param("bloodType") BloodType bloodType, @Param("delta") int delta);
}
