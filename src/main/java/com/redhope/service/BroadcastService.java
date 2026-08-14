package com.redhope.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.redhope.dto.CriticalBroadcastDTO;
import com.redhope.entity.BloodRequest;

@Service
public class BroadcastService {

    private static final Logger logger = LoggerFactory.getLogger(BroadcastService.class);

    private static final long BROADCAST_TIMEOUT_HOURS = 24;

    private final Map<Long, CriticalBroadcastDTO> activeBroadcasts = new ConcurrentHashMap<>();

    public void addBroadcast(BloodRequest request) {
        CriticalBroadcastDTO dto = new CriticalBroadcastDTO(request);
        activeBroadcasts.put(request.getId(), dto);
        logger.info("Broadcast added: request #{} (hospital={}, bloodType={})",
                request.getId(),
                request.getHospital().getName(),
                request.getBloodType().getDisplayName());
    }

    public void deactivateBroadcast(Long requestId) {
        CriticalBroadcastDTO broadcast = activeBroadcasts.get(requestId);
        if (broadcast != null) {
            broadcast.setActive(false);
            broadcast.setDeactivatedAt(LocalDateTime.now());
            activeBroadcasts.remove(requestId);
            logger.info("Broadcast deactivated: request #{}", requestId);
        }
    }

    public void deactivateExpiredBroadcasts() {
        LocalDateTime now = LocalDateTime.now();
        activeBroadcasts.values().removeIf(broadcast -> {
            if (broadcast.getCreatedAt() != null && broadcast.getCreatedAt().plusHours(BROADCAST_TIMEOUT_HOURS).isBefore(now)) {
                broadcast.setActive(false);
                broadcast.setDeactivatedAt(now);
                logger.info("Broadcast auto-deactivated (timeout): request #{}", broadcast.getRequestId());
                return true;
            }
            return false;
        });
    }

    public List<CriticalBroadcastDTO> getActiveBroadcasts() {
        deactivateExpiredBroadcasts();
        return activeBroadcasts.values().stream()
                .filter(CriticalBroadcastDTO::isActive)
                .collect(Collectors.toList());
    }
}
