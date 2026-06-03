package com.healthydiet.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.healthydiet.system.dto.profile.ProfileRequest;
import com.healthydiet.system.dto.profile.ProfileResponse;
import com.healthydiet.system.dto.profile.ProfileMetricsResponse;
import com.healthydiet.system.entity.UserProfile;

public interface UserProfileService extends IService<UserProfile> {

    ProfileResponse getProfile(Long userId);

    ProfileResponse saveProfile(Long userId, ProfileRequest request);

    ProfileMetricsResponse getMetrics(Long userId);
}