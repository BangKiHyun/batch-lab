package com.spring.batch.lab.readbook.chap4.step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Callable;

@Configuration
@RequiredArgsConstructor
public class CallableTaskletConfiguration {

    public static final String JOB_NAME = "callable_job";
    public static final String STEP_NAME = "-callable_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job callableJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(callableStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step callableStep() {
        return this.stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet(tasklet())
                .build();

    }

    @Bean(name = "callable_tasklet")
    public CallableTaskletAdapter tasklet() {
        CallableTaskletAdapter taskletAdapter = new CallableTaskletAdapter();
        taskletAdapter.setCallable(callableObject());
        return taskletAdapter;
    }

    @Bean
    public Callable<RepeatStatus> callableObject() {
        return () -> {
            System.out.println("Hello. This is callable tasklet");
            System.out.println("This was executed in another thread");
            return RepeatStatus.FINISHED;
        };
    }
}
