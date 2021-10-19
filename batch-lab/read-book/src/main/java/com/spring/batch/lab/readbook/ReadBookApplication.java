package com.spring.batch.lab.readbook;

import com.spring.batch.lab.readbook.config.BatchConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(value = {BatchConfiguration.class})
public class ReadBookApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReadBookApplication.class, args);
    }

}
