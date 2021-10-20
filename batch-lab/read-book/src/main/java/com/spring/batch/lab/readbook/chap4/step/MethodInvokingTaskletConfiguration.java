package com.spring.batch.lab.readbook.chap4.step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MethodInvokingTaskletConfiguration {

    public static final String JOB_NAME = "chap4_methodInvoking_job";
    public static final String STEP_NAME = "-chap4_methodInvoking_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(methodInvokingTaskletStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step methodInvokingStep() {
        return stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet(methodInvokingTasklet())
                .build();
    }

    @Bean
    public MethodInvokingTaskletAdapter methodInvokingTasklet() {
        final MethodInvokingTaskletAdapter taskletAdapter = new MethodInvokingTaskletAdapter();
        taskletAdapter.setTargetObject(service());
        taskletAdapter.setTargetMethod("serviceMethod");
        return taskletAdapter;
    }

    @Bean
    public CustomeService service() {
        return new CustomeService();
    }
}
