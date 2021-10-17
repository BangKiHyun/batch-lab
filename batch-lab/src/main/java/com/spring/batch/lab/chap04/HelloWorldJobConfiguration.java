package com.spring.batch.lab.chap04;

import com.spring.batch.lab.chap04.incrementer.DailyJobTimestamper;
import com.spring.batch.lab.chap04.listener.JobLoggerListenerV1;
import com.spring.batch.lab.chap04.listener.JobLoggerListenerV2;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class HelloWorldJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final CompositeJobParametersValidator validator;

    public static final String JOB_NAME = "helloJob";
    public static final String STEP_NAME = "helloStep";

    @Bean(name = JOB_NAME)
    public Job job() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(step1())
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .incrementer(new DailyJobTimestamper())
                .listener(new JobLoggerListenerV1())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListenerV2()))
                .build();
    }

    @Bean(name = STEP_NAME + "FIRST")
    public Step step1() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .tasklet(helloWorldTasklet(null, null))
                .build();
    }

    // chunkContext 사용
    @Bean
    public Tasklet helloWorldTasklet() {
        return (contribution, chunkContext) -> {
            String name = (String) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("name");
            System.out.println(String.format("Hello, %s!", name));
            return RepeatStatus.FINISHED;
        };
    }

    // late binding
    @StepScope
    @Bean
    public Tasklet helloWorldTasklet(@Value("#{jobParameters['fileName']}") String fileName,
                                     @Value("#{jobParameters['name']}") String name) {
        return ((contribution, chunkContext) -> {
            System.out.println(String.format("Hello, %s!", name));

            System.out.println(String.format("fileName = %s", fileName));
            return RepeatStatus.FINISHED;
        });
    }
}
