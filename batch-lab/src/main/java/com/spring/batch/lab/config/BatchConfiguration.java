package com.spring.batch.lab.config;

import com.spring.batch.lab.launcer.TestJobLauncher;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@ComponentScan(value = {
        "com.spring.batch.lab.chap02",
        "com.spring.batch.lab.chap04",
        "com.spring.batch.lab.chap13",
})
public class BatchConfiguration {

    @Bean
    public TestJobLauncher testJobLauncher(
            ApplicationContext applicationContext,
            JobRepository jobRepository,
            JobLauncher jobLauncher) {
        return new TestJobLauncher(applicationContext, jobRepository, jobLauncher);
    }
}
