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
import org.springframework.batch.item.database.HibernateCursorItemReader;
import org.springframework.batch.item.database.builder.HibernateCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class HibernateCursorJobConfiguration {

    public static final String JOB_NAME = "chap7_hibernate_cursor_job";
    public static final String STEP_NAME = "chap7_hibernate_cursor_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job hibernateCursorJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(hibernateCursorStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step hibernateCursorStep() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<CustomerJPA, CustomerJPA>chunk(10)
                .reader(hibernateCursorItemReader(null, null))
                .writer(hibernateCursorItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public HibernateCursorItemReader<CustomerJPA> hibernateCursorItemReader(EntityManagerFactory entityManagerFactory,
                                                                            @Value("#{jobParameters['city']}") String city) {
        return new HibernateCursorItemReaderBuilder<CustomerJPA>()
                .name("hibernateCursorItemReader")
                .sessionFactory(entityManagerFactory.unwrap(SessionFactory.class))
                .queryString("FROM CustomerJPA WHERE city =: city")
                .parameterValues(Collections.singletonMap("city", city))
                .build();
    }

    @Bean
    public ItemWriter<CustomerJPA> hibernateCursorItemWriter() {
        return (items) -> items.forEach(System.out::println);
    }
}
