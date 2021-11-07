package com.spring.batch.lab.readbook.chap6.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RestJobConfiguration {

    public static final String JOB_NAME = "chap6_rest_job";
    public static final String STEP_NAME = "-chap6_rest_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job job() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .start(restStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step restStep() {
        return this.stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("first step ran today");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
