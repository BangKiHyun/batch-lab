package com.spring.batch.lab.testfixture.test.config;

import com.spring.batch.lab.testfixture.test.TestJobLauncher;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Configuration
public class BatchTestConfig {
    @Bean
    public TestJobLauncher testJobLauncher(
            ApplicationContext applicationContext,
            JobRepository jobRepository,
            JobLauncher jobLauncher) {
        return new TestJobLauncher(applicationContext, jobRepository, jobLauncher);
    }
}
