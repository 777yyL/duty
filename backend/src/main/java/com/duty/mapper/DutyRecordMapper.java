package com.duty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duty.entity.DutyRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 值班记录Mapper接口
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Mapper
public interface DutyRecordMapper extends BaseMapper<DutyRecord> {

}
