package com.healthydiet.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthydiet.system.entity.User;
import com.healthydiet.system.mapper.UserMapper;
import com.healthydiet.system.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}