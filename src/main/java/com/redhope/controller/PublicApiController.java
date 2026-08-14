package com.redhope.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redhope.dto.CriticalBroadcastDTO;
import com.redhope.service.BroadcastService;

@RestController
public class PublicApiController {

    private final BroadcastService broadcastService;

    public PublicApiController(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @GetMapping("/api/public/critical-broadcasts")
    public List<CriticalBroadcastDTO> getCriticalBroadcasts() {
        return broadcastService.getActiveBroadcasts();
    }
}
