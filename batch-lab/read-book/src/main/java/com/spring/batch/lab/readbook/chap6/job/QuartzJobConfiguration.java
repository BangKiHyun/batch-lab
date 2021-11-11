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
public class QuartzJobConfiguration {

    public static final String JOB_NAME = "chap6_quartz_job";
    public static final String STEP_NAME = "-chap6_quartz_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job quartzJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .incrementer(new RunIdIncrementer()) // 증분기 사용 여부 중요
                .start(step1())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step step1() {
        return this.stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("use quartz scheduler step ran!");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
