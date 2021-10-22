package com.spring.batch.lab.readbook.chap4.step.condition;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FlowJobConfiguration {

    public static final String JOB_NAME = "chap4_flow_job";
    public static final String STEP_NAME = "-chap4_flow_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job flowJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(preProcessingFlow())
//                .start(wrappingFlowStep())
                .next(runBatch())
                .end()
                .build();
    }

    /**
     * 플로우 레핑 Step
     */
    @Bean
    public Step wrappingFlowStep() {
        return this.stepBuilderFactory.get("wrappingFlowStep")
                .job(preProcessingJob()) //서브 잡 (지양하는 방법)
                .parametersExtractor(new DefaultJobParametersExtractor())
                .build();
    }

    @Bean
    public Job preProcessingJob() {
        return this.jobBuilderFactory.get("preProcessingJob")
                .start(loadCustomerStep())
                .next(loadCustomerStep())
                .next(updateStartStep())
                .build();
    }

    @Bean(name = "processing" + STEP_NAME)
    public Flow preProcessingFlow() {
        return new FlowBuilder<Flow>("processing" + STEP_NAME)
                .start(loadFileStep())
                .next(loadCustomerStep())
                .next(updateStartStep())
                .build();
    }

    @Bean(name = "run" + STEP_NAME)
    public Step runBatch() {
        return this.stepBuilderFactory.get("run" + STEP_NAME)
                .tasklet(runBatchTasklet())
                .build();
    }

    @Bean
    public Step loadFileStep() {
        return this.stepBuilderFactory.get("loadFileStep")
                .tasklet(loadStockFile())
                .build();
    }

    @Bean
    public Step loadCustomerStep() {
        return this.stepBuilderFactory.get("loadCustomerStep")
                .tasklet(loadCustomerFile())
                .build();
    }

    @Bean
    public Step updateStartStep() {
        return this.stepBuilderFactory.get("updateStartStep")
                .tasklet(updateStart())
                .build();
    }

    @Bean
    public Tasklet runBatchTasklet() {
        return ((contribution, chunkContext) -> {
            System.out.println("The batch has been run");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Tasklet loadStockFile() {
        return ((contribution, chunkContext) -> {
            System.out.println("The stock file has been loaded");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Tasklet loadCustomerFile() {
        return ((contribution, chunkContext) -> {
            System.out.println("The customer file has been loaded");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Tasklet updateStart() {
        return ((contribution, chunkContext) -> {
            System.out.println("The start has been updated");
            return RepeatStatus.FINISHED;
        });
    }
}
