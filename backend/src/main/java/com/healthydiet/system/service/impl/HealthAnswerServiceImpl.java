package com.healthydiet.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthydiet.system.entity.HealthAnswer;
import com.healthydiet.system.mapper.HealthAnswerMapper;
import com.healthydiet.system.service.HealthAnswerService;
import org.springframework.stereotype.Service;

@Service
public class HealthAnswerServiceImpl extends ServiceImpl<HealthAnswerMapper, HealthAnswer> implements HealthAnswerService {
}