package com.healthydiet.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthydiet.system.common.BusinessException;
import com.healthydiet.system.dto.auth.AuthUserResponse;
import com.healthydiet.system.dto.auth.LoginRequest;
import com.healthydiet.system.dto.auth.LoginResponse;
import com.healthydiet.system.dto.auth.RegisterRequest;
import com.healthydiet.system.entity.User;
import com.healthydiet.system.security.JwtClaims;
import com.healthydiet.system.security.JwtTokenProvider;
import com.healthydiet.system.service.AuthService;
import com.healthydiet.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        boolean exists = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0;
        if (exists) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : username);
        userService.save(user);
        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername().trim())
                .last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return buildLoginResponse(user);
    }

    @Override
    public AuthUserResponse getCurrentUser(JwtClaims claims) {
        User user = userService.getById(claims.getUserId());
        if (user == null) {
            throw new BusinessException(401, "用户不存在或已被删除");
        }
        return AuthUserResponse.from(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername());
        return new LoginResponse(token, AuthUserResponse.from(user));
    }
}