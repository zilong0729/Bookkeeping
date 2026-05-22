package com.bookkeeping;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 微信小程序个人记账系统启动类
 */
@SpringBootApplication
@MapperScan("com.bookkeeping.mapper")
@EnableScheduling
public class BookkeepingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookkeepingApplication.class, args);
    }
}
