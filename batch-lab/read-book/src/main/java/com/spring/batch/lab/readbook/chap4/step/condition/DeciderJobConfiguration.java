package com.spring.batch.lab.readbook.chap4.step.condition;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DeciderJobConfiguration {

    public static final String JOB_NAME = "chap4_decider_job";
    public static final String STEP_NAME = "-chap4_decider_step";
    public static final String TASKLET_NAME = "-chap4_decider_tasklet";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job deciderJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(firstStep())
                .next(decider())
                .from(decider())
                .on("FAILED").to(failureStep())
                .from(decider())
                .on("*").to(successStep())
                .end()
                .build();
    }

    @Bean(name = "decider" + STEP_NAME)
    public JobExecutionDecider decider() {
        return new RandomDecider();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step firstStep() {
        return this.stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet(passTasklet())
                .build();
    }

    @Bean(name = "success" + STEP_NAME)
    public Step successStep() {
        return this.stepBuilderFactory.get("success" + STEP_NAME)
                .tasklet(successTasklet())
                .build();
    }

    @Bean(name = "failure" + STEP_NAME)
    public Step failureStep() {
        return this.stepBuilderFactory.get("failure" + STEP_NAME)
                .tasklet(failTasklet())
                .build();
    }

    @Bean(name = "pass" + TASKLET_NAME)
    public Tasklet passTasklet() {
        return (((contribution, chunkContext) -> {
//            throw new RuntimeException("This is a failure");
            return RepeatStatus.FINISHED;
        }));
    }

    @Bean(name = "success" + TASKLET_NAME)
    public Tasklet successTasklet() {
        return ((contribution, chunkContext) -> {
            System.out.println("Success Tasklet!");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean(name = "fail" + TASKLET_NAME)
    public Tasklet failTasklet() {
        return ((contribution, chunkContext) -> {
            System.out.println("Failure Tasklet!");
            return RepeatStatus.FINISHED;
        });
    }
}
