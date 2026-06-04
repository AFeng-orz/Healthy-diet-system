package com.healthydiet.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthydiet.system.entity.DietRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DietRecordMapper extends BaseMapper<DietRecord> {
}
