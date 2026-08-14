package com.redhope.listener;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.redhope.entity.BloodRequest;
import com.redhope.entity.User;
import com.redhope.enums.Role;
import com.redhope.event.CriticalRequestEvent;
import com.redhope.repository.UserRepository;
import com.redhope.service.BroadcastService;
import com.redhope.service.NotificationService;

@Component
public class BroadcastEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BroadcastEventListener.class);

    private final BroadcastService broadcastService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private final Set<Long> criticalEmailsSent = ConcurrentHashMap.newKeySet();

    public BroadcastEventListener(BroadcastService broadcastService,
                                  NotificationService notificationService,
                                  UserRepository userRepository) {
        this.broadcastService = broadcastService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @EventListener
    public void handleCriticalRequest(CriticalRequestEvent event) {
        BloodRequest request = event.getBloodRequest();

        logger.info("Critical broadcast triggered: Hospital '{}' requesting {} blood (request #{})",
                request.getHospital().getName(),
                request.getBloodType().getDisplayName(),
                request.getId());

        broadcastService.addBroadcast(request);

        if (criticalEmailsSent.add(request.getId())) {
            try {
                List<User> matchingDonors = userRepository.findActiveDonorsByBloodTypeAndCity(
                        Role.ROLE_USER,
                        request.getBloodType(),
                        request.getHospital().getCity()
                );
                notificationService.notifyMatchingDonorsOfCriticalRequest(request, matchingDonors);
            } catch (Exception e) {
                logger.error("Failed to send critical donor emails for request #{}: {}", request.getId(), e.getMessage());
            }
        } else {
            logger.info("Skipping duplicate critical donor emails for request #{}", request.getId());
        }

        logger.info("Critical broadcast active for request #{}", request.getId());
    }
}
