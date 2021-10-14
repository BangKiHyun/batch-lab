package com.spring.batch.lab.chap02;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HelloWorldBatchConfiguration {

    public static final String JOB_NAME = "job";
    public static final String STEP_NAME = JOB_NAME + "-step1";

    private final JobBuilderFactory jobFactory;
    private final StepBuilderFactory stepFactory;

    @Bean(name = JOB_NAME)
    public Job job() {
        return this.jobFactory.get(JOB_NAME)
                .start(step1())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step step1() {
        return this.stepFactory.get(STEP_NAME)
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("Hello World!");
                    return RepeatStatus.FINISHED;
                })).build();
    }
}
