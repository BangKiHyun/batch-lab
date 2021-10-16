package com.spring.batch.lab;

import com.spring.batch.lab.config.BatchConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(value = BatchConfiguration.class)
public class BatchLabApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchLabApplication.class, args);
    }
}
