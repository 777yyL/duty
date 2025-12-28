package com.duty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duty.entity.DutySchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班Mapper接口
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Mapper
public interface DutyScheduleMapper extends BaseMapper<DutySchedule> {

    /**
     * 查询日期范围内的排班列表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班列表
     */
    @Select("SELECT * FROM duty_schedule WHERE deleted = 0 AND (status IS NULL OR status = 1) AND duty_date BETWEEN #{startDate} AND #{endDate} ORDER BY duty_date, shift_id")
    List<DutySchedule> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期的排班列表
     *
     * @param dutyDate 值班日期
     * @return 排班列表
     */
    @Select("SELECT * FROM duty_schedule WHERE deleted = 0 AND (status IS NULL OR status = 1) AND duty_date = #{dutyDate} ORDER BY shift_id")
    List<DutySchedule> selectByDate(@Param("dutyDate") LocalDate dutyDate);

    /**
     * 逻辑删除指定日期的所有排班
     *
     * @param dutyDate 值班日期
     * @return 删除数量
     */
    @Update("UPDATE duty_schedule SET deleted = 1 WHERE duty_date = #{dutyDate}")
    int deleteByDate(@Param("dutyDate") LocalDate dutyDate);
}
