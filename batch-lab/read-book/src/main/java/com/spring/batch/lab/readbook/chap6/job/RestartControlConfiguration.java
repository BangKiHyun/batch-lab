package com.spring.batch.lab.readbook.chap6.job;

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
public class RestartControlConfiguration {

    public static final String JOB_NAME = "chap6_prevent_restart_job";
    public static final String STEP_NAME = "-chap6_prevent_restart_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job preventRestartJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .preventRestart()
                .start(step1())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step step1() {
        return this.stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("ran prevent restart step!");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean(name = "second" + STEP_NAME)
    public Step step2() {
        return this.stepBuilderFactory.get("second" + STEP_NAME)
                .startLimit(2)
                .tasklet((contribution, chunkContext) -> {
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean(name = "third" + STEP_NAME)
    public Step step3() {
        return this.stepBuilderFactory.get("third" + STEP_NAME)
                .allowStartIfComplete(true)
                .tasklet((contribution, chunkContext) -> {
                    return RepeatStatus.FINISHED;
                }).build();
    }
}