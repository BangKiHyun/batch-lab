package com.spring.batch.lab.readbook.chap4.step;

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
public class LambdaTaskletConfiguration {

    public static final String JOB_NAME = "chap4_tasklet_job";
    public static final String STEP_NAME = "-chap4_tasklet_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(lambdaTaskletStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step lambdaTaskletStep() {
        return stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("Hello! Lambda Tasklet");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }
}
