package com.spring.batch.lab.readbook.chap4.step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class ChunkPolicyJobConfiguration {

    public static final String JOB_NAME = "chap4_chunkPolicy_job";
    public static final String STEP_NAME = "-chap4_chunkPolicy_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job chunkPolicyJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(chunkPolicyStep())
//                .next(compositePolicyStep())
//                .next(randomPolicyStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step chunkPolicyStep() {
        return this.stepBuilderFactory.get("first" + STEP_NAME)
                .<String, String>chunk(1000)
                .reader(itemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean(name = "second" + STEP_NAME)
    public Step randomPolicyStep() {
        return this.stepBuilderFactory.get("second" + STEP_NAME)
                .<String, String>chunk(new RandomChunkSizePolicy())
                .reader(itemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean(name = "composite" + STEP_NAME)
    public Step compositePolicyStep() {
        return this.stepBuilderFactory.get("composite" + STEP_NAME)
                .<String, String>chunk(completionPolicy())
                .reader(itemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public CompletionPolicy completionPolicy() {
        CompositeCompletionPolicy policy = new CompositeCompletionPolicy();
        policy.setPolicies(new CompletionPolicy[]{
                new TimeoutTerminationPolicy(3),
                new SimpleCompletionPolicy(1000)});
        return policy;
    }

    @Bean(name = "policyItemReader")
    public ListItemReader<String> itemReader() {
        final List<String> itmes = new ArrayList<>(100_000);
        for (int idx = 0; idx < 100_000; idx++) {
            itmes.add(UUID.randomUUID().toString());
        }
        return new ListItemReader<>(itmes);
    }

    @Bean(name = "policyItemWriter")
    public ItemWriter<String> itemWriter() {
        return items -> {
            for (String item : items) {
                System.out.println(">> current item = " + item);
            }
        };
    }
}
