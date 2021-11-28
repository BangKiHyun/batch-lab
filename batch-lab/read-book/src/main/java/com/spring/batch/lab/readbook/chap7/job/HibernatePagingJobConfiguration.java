package com.spring.batch.lab.readbook.chap7.job;

import com.spring.batch.lab.readbook.chap7.model.CustomerJPA;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.HibernatePagingItemReader;
import org.springframework.batch.item.database.builder.HibernatePagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class HibernatePagingJobConfiguration {

    public static final String JOB_NAME = "chap7_hibernate_paging_job";
    public static final String STEP_NAME = "chap7_hibernate_paging_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job hibernatePagingJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(hibernatePagingStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step hibernatePagingStep() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<CustomerJPA, CustomerJPA>chunk(10)
                .reader(hibernatePagingItemReader(null, null))
                .writer(hibernatePagingItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public HibernatePagingItemReader<CustomerJPA> hibernatePagingItemReader(EntityManagerFactory entityManagerFactory,
                                                                            @Value("#{jobParameters['city']}") String city) {
        return new HibernatePagingItemReaderBuilder<CustomerJPA>()
                .name("hibernatePagingItemReader")
                .sessionFactory(entityManagerFactory.unwrap(SessionFactory.class))
                .queryString("FROM CustomerJPA WHERE city =: city")
                .parameterValues(Collections.singletonMap("city", city))
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemWriter<CustomerJPA> hibernatePagingItemWriter() {
        return (items) -> items.forEach(System.out::println);
    }
}
