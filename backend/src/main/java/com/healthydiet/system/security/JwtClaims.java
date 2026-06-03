package com.healthydiet.system.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtClaims {

    private Long userId;

    private String username;
}