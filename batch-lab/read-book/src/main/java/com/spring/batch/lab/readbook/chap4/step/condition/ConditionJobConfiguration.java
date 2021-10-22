package com.spring.batch.lab.readbook.chap4.step.condition;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ConditionJobConfiguration {

    public static final String JOB_NAME = "chap4_condition_job";
    public static final String STEP_NAME = "-chap4_condition_step";

    public static final String FAIL_EXIT_STATUS = "FAILED";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    /**
     * firstStep 결과가 정상이면 successStep 실행
     * firstStep이 ExiStatus.FAILED 반환하면 failureStep 실행
     */
    @Bean(name = JOB_NAME)
    public Job conditionJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(firstStep())
//                .on(FAIL_EXIT_STATUS).end()
//                .on(FAIL_EXIT_STATUS).fail()
                .on(FAIL_EXIT_STATUS).stopAndRestart(successStep())
                .on(FAIL_EXIT_STATUS).to(failureStep())
                .from(firstStep()).on("*").to(successStep())
                .end()
                .build();
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

    @Bean
    public Tasklet passTasklet() {
        return (((contribution, chunkContext) -> {
            throw new RuntimeException("This is a failure");
//            return RepeatStatus.FINISHED;
        }));
    }

    @Bean
    public Tasklet successTasklet() {
        return ((contribution, chunkContext) -> {
            System.out.println("Success Tasklet!");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Tasklet failTasklet() {
        return ((contribution, chunkContext) -> {
            System.out.println("Failure Tasklet!");
            return RepeatStatus.FINISHED;
        });
    }
}
