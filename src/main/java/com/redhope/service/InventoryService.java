package com.redhope.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redhope.entity.BloodInventory;
import com.redhope.entity.Hospital;
import com.redhope.entity.User;
import com.redhope.enums.BloodType;
import com.redhope.repository.BloodInventoryRepository;
import com.redhope.repository.HospitalRepository;

@Service
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final BloodInventoryRepository bloodInventoryRepository;
    private final HospitalRepository hospitalRepository;

    public InventoryService(BloodInventoryRepository bloodInventoryRepository,
                            HospitalRepository hospitalRepository) {
        this.bloodInventoryRepository = bloodInventoryRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @Transactional(readOnly = true)
    public List<BloodInventory> getInventoryForHospital(Hospital hospital) {
        List<BloodInventory> inventory = bloodInventoryRepository.findByHospitalOrderByBloodTypeAsc(hospital);
        ensureAllBloodTypesExist(hospital, inventory);
        return bloodInventoryRepository.findByHospitalOrderByBloodTypeAsc(hospital);
    }

    public BloodInventory updateStock(Hospital hospital, BloodType bloodType, int newUnits, User admin) {
        if (newUnits < 0) {
            throw new IllegalArgumentException("Stock units cannot be negative.");
        }

        BloodInventory inventory = bloodInventoryRepository
                .findByHospitalAndBloodType(hospital, bloodType)
                .orElseGet(() -> createInventoryRow(hospital, bloodType));

        int oldUnits = inventory.getUnitsAvailable();
        inventory.setUnitsAvailable(newUnits);
        BloodInventory saved = bloodInventoryRepository.save(inventory);

        logger.info("Inventory updated for hospital={}, bloodType={}, oldUnits={}, newUnits={}",
                hospital.getName(), bloodType, oldUnits, newUnits);

        return saved;
    }

    public BloodInventory updateThreshold(Hospital hospital, BloodType bloodType, int newThreshold, User admin) {
        if (newThreshold < 0) {
            throw new IllegalArgumentException("Low stock threshold cannot be negative.");
        }

        BloodInventory inventory = bloodInventoryRepository
                .findByHospitalAndBloodType(hospital, bloodType)
                .orElseGet(() -> createInventoryRow(hospital, bloodType));

        int oldThreshold = inventory.getLowStockThreshold();
        inventory.setLowStockThreshold(newThreshold);
        BloodInventory saved = bloodInventoryRepository.save(inventory);

        logger.info("Threshold updated for hospital={}, bloodType={}, oldThreshold={}, newThreshold={}",
                hospital.getName(), bloodType, oldThreshold, newThreshold);

        return saved;
    }

    private void ensureAllBloodTypesExist(Hospital hospital, List<BloodInventory> existing) {
        if (existing.size() == BloodType.values().length) {
            return;
        }

        Map<BloodType, BloodInventory> existingMap = existing.stream()
                .collect(Collectors.toMap(BloodInventory::getBloodType, bi -> bi));

        for (BloodType type : BloodType.values()) {
            if (!existingMap.containsKey(type)) {
                createInventoryRow(hospital, type);
            }
        }
    }

    private BloodInventory createInventoryRow(Hospital hospital, BloodType bloodType) {
        BloodInventory inventory = new BloodInventory();
        inventory.setHospital(hospital);
        inventory.setBloodType(bloodType);
        inventory.setUnitsAvailable(0);
        inventory.setLowStockThreshold(5);
        return bloodInventoryRepository.save(inventory);
    }
}
