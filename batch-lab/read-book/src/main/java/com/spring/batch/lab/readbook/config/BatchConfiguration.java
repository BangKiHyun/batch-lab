package com.spring.batch.lab.readbook.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@ComponentScan(basePackages = "com.spring.batch.lab.readbook")
public class BatchConfiguration {
}
