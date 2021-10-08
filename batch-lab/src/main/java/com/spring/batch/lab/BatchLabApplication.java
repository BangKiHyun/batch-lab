package com.spring.batch.lab;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication
public class BatchLabApplication {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    private static final String JOB_NAME = "job";
    private static final String STEP_NAME = JOB_NAME + "-step1";

    @Bean(name = JOB_NAME)
    public Job job() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(step1())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step step1() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Hello World!");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    public static void main(String[] args) {
        SpringApplication.run(BatchLabApplication.class, args);
    }

}
