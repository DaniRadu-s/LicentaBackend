package com.example.demo.service;

import com.example.demo.domain.dto.RestrictionDTO;
import com.example.demo.domain.dto.UserProfileDTO;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.UserAvailableDay;
import com.example.demo.domain.entity.UserProfile;
import com.example.demo.domain.entity.UserRestriction;
import com.example.demo.persistence.UserAvailableDayRepository;
import com.example.demo.persistence.UserProfileRepository;
import com.example.demo.persistence.UserRepository;
import com.example.demo.persistence.UserRestrictionRepository;
import com.example.demo.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfileService {

    private final UserRepository userRepo;
    private final UserProfileRepository profileRepo;
    private final UserAvailableDayRepository dayRepo;
    private final UserRestrictionRepository restrictionRepo;

    public ProfileService(UserRepository userRepo,
                          UserProfileRepository profileRepo,
                          UserAvailableDayRepository dayRepo,
                          UserRestrictionRepository restrictionRepo) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.dayRepo = dayRepo;
        this.restrictionRepo = restrictionRepo;
    }

    private Long currentUserId() {
        String usernameOrEmail = SecurityUtils.getCurrentUsername();
        User u = userRepo.findByEmail(usernameOrEmail)
                .orElseThrow();
        return u.getId();
    }

    @Transactional
    public void upsert(UserProfileDTO dto) {
        Long userId = currentUserId();

        UserProfile profile = profileRepo.findById(userId).orElseGet(() -> {
            UserProfile p = new UserProfile();
            p.setUserId(userId);
            return p;
        });

        profile.setAge(dto.age());
        profile.setWeight(dto.weight());
        profile.setHeightCm(dto.heightCm());
        profile.setSex(dto.sex());
        profile.setExperienceLevel(dto.experienceLevel());
        profile.setPrimaryGoal(dto.primaryGoal());
        profile.setMaxWorkoutMinutes(dto.maxWorkoutMinutes());
        profile.setEquipment(dto.equipment());

        profileRepo.save(profile);

        dayRepo.deleteByIdUserId(userId);
        if (dto.availableDays() != null) {
            List<UserAvailableDay> days = dto.availableDays().stream()
                    .distinct()
                    .map(day -> new UserAvailableDay(userId, day))
                    .toList();
            dayRepo.saveAll(days);
        }

        restrictionRepo.deleteByUserId(userId);
        if (dto.restrictions() != null) {
            List<UserRestriction> restrictions = dto.restrictions().stream()
                    .map(r -> {
                        UserRestriction ur = new UserRestriction();
                        ur.setUserId(userId);
                        ur.setRestrictionType(r.restrictionType());
                        ur.setDescription(r.description());
                        return ur;
                    })
                    .toList();
            restrictionRepo.saveAll(restrictions);
        }
    }

    @Transactional(readOnly = true)
    public UserProfileDTO get() {
        Long userId = currentUserId();

        List<String> days = dayRepo.findByIdUserId(userId).stream()
                .map(d -> d.getId().getDayOfWeek())
                .toList();

        List<RestrictionDTO> restrictions = restrictionRepo.findByUserId(userId).stream()
                .map(r -> new RestrictionDTO(r.getRestrictionType(), r.getDescription()))
                .toList();

        UserProfile p = profileRepo.findById(userId).orElse(null);

        if (p == null) {

            return new UserProfileDTO(
                    null, null,null, null,
                    "BEGINNER",
                    "STRENGTH",
                    null,
                    "GYM",
                    days,
                    restrictions
            );
        }

        return new UserProfileDTO(
                p.getAge(),
                p.getWeight(),
                p.getHeightCm(),
                p.getSex(),
                p.getExperienceLevel(),
                p.getPrimaryGoal(),
                p.getMaxWorkoutMinutes(),
                p.getEquipment(),
                days,
                restrictions
        );
    }
}
