package com.healthydiet.system.dto.profile;

import com.healthydiet.system.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileResponse {

    private UserProfile profile;

    private ProfileMetricsResponse metrics;
}