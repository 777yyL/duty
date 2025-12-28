package com.duty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 值班看板系统主启动类
 *
 * @author duty-system
 * @since 2025-12-26
 */
@SpringBootApplication
@MapperScan("com.duty.mapper")
public class DutyRosterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DutyRosterApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("值班看板系统启动成功！");
        System.out.println("访问地址: http://localhost:8080/api");
        System.out.println("========================================\n");
    }
}
