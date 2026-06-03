package com.healthydiet.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthydiet.system.common.BusinessException;
import com.healthydiet.system.dto.profile.ProfileMetricsResponse;
import com.healthydiet.system.dto.profile.ProfileRequest;
import com.healthydiet.system.dto.profile.ProfileResponse;
import com.healthydiet.system.entity.UserProfile;
import com.healthydiet.system.mapper.UserProfileMapper;
import com.healthydiet.system.service.NutritionCalculator;
import com.healthydiet.system.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    private final NutritionCalculator nutritionCalculator;

    @Override
    public ProfileResponse getProfile(Long userId) {
        UserProfile profile = findByUserId(userId);
        return buildResponse(profile);
    }

    @Override
    @Transactional
    public ProfileResponse saveProfile(Long userId, ProfileRequest request) {
        UserProfile profile = findByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }

        profile.setGender(request.getGender());
        profile.setAge(request.getAge());
        profile.setHeightCm(request.getHeightCm());
        profile.setWeightKg(request.getWeightKg());
        profile.setTargetWeightKg(request.getTargetWeightKg());
        profile.setActivityLevel(request.getActivityLevel());
        profile.setDietGoal(request.getDietGoal());
        profile.setAvoidFoods(cleanText(request.getAvoidFoods()));
        profile.setAllergies(cleanText(request.getAllergies()));
        profile.setPreferences(cleanText(request.getPreferences()));

        saveOrUpdate(profile);
        return buildResponse(profile);
    }

    @Override
    public ProfileMetricsResponse getMetrics(Long userId) {
        UserProfile profile = findByUserId(userId);
        if (profile == null) {
            throw new BusinessException("请先完善健康档案");
        }
        return nutritionCalculator.calculate(profile);
    }

    private UserProfile findByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId)
                .last("LIMIT 1"));
    }

    private ProfileResponse buildResponse(UserProfile profile) {
        if (profile == null) {
            return new ProfileResponse(null, null);
        }
        return new ProfileResponse(profile, nutritionCalculator.calculate(profile));
    }

    private String cleanText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}