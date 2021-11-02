package com.spring.batch.lab.readbook.chap5;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JobExplorerConfiguration {

    public static final String JOB_NAME = "chap5_explorer_job";
    public static final String STEP_NAME = "-chap5_explorer_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobExplorer jobExplorer;

    @Bean(name = JOB_NAME)
    public Job explorerJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(explorerStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step explorerStep() {
        return this.stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet(explorerTasklet())
                .build();
    }

    @Bean
    public Tasklet explorerTasklet() {
        return new ExploringTasklet(this.jobExplorer);
    }
}
