package com.example.demo.controller;

import com.example.demo.domain.dto.UserProfileDTO;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.ProfileService;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public UserProfileDTO get() {
        return profileService.get();
    }

    @PutMapping
    public void put(@RequestBody UserProfileDTO dto) {
        profileService.upsert(dto);
    }
}
