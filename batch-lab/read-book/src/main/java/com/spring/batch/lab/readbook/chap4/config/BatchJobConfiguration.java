package com.spring.batch.lab.readbook.chap4.config;

import com.spring.batch.lab.readbook.chap4.tasklet.HelloTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BatchJobConfiguration {

    public static final String JOB_NAME = "chap4_job";
    public static final String STEP_NAME = "-chap4_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job job() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(step1())
                .next(step2())
                .build();
    }

    @Bean(name = "one" + STEP_NAME)
    public Step step1() {
        return this.stepBuilderFactory.get("one" + STEP_NAME)
                .tasklet(new HelloTasklet())
                .listener(promotionListener())
                .build();
    }

    @Bean(name = "two" + STEP_NAME)
    public Step step2() {
        return this.stepBuilderFactory.get("two" + STEP_NAME)
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("Good Bye");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    // step의 ExecutionContext에서 "name"키를 찾으면 Job의 ExecutionContext에 복사 (Step -> Job ExecutionContext로 승격)
    @Bean
    public StepExecutionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"name"});
        return listener;
    }
}
