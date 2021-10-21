package com.spring.batch.lab.readbook.chap4.step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class ChunkJobConfiguration {

    public static final String JOB_NAME = "chap4_chunk_job";
    public static final String STEP_NAME = "-chap4_chunk_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job chunkJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(chunkStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step chunkStep() {
        return this.stepBuilderFactory.get("first" + STEP_NAME)
                .<String, String>chunk(1000)
                .reader(itemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean(name = "chunkItemReader")
    public ListItemReader<String> itemReader() {
        final List<String> itmes = new ArrayList<>(100_000);
        for (int idx = 0; idx < 100_000; idx++) {
            itmes.add(UUID.randomUUID().toString());
        }
        return new ListItemReader<>(itmes);
    }

    @Bean(name = "chunkItemWriter")
    public ItemWriter<String> itemWriter() {
        return items -> {
            for (String item : items) {
                System.out.println(">> current item = " + item);
            }
        };
    }
}
