package com.healthydiet.system.dto.auth;

import com.healthydiet.system.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthUserResponse {

    private Long id;

    private String username;

    private String nickname;

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(user.getId(), user.getUsername(), user.getNickname());
    }
}