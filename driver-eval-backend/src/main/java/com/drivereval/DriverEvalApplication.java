package com.drivereval;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.drivereval.mapper")
public class DriverEvalApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriverEvalApplication.class, args);
    }
}
