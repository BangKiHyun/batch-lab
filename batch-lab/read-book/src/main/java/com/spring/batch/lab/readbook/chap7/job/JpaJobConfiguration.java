package com.spring.batch.lab.readbook.chap7.job;

import com.spring.batch.lab.readbook.chap7.model.CustomerJPA;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class JpaJobConfiguration {

    public static final String JOb_NAME = "chap7_jpa_job";
    public static final String STEP_NAME = "chap7_jpa_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOb_NAME)
    public Job jpaJob() {
        return this.jobBuilderFactory.get(JOb_NAME)
                .start(jpaStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step jpaStep() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<CustomerJPA, CustomerJPA>chunk(10)
                .reader(customerJpaItemReader(null, null))
                .writer(jpaItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<CustomerJPA> customerJpaItemReader(
            EntityManagerFactory entityManagerFactory,
            @Value("#{jobParameters['city']}") String city) {
        return new JpaPagingItemReaderBuilder<CustomerJPA>()
                .name("customerJpaItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM CustomerJPA c WHERE c.city = :city")
                .parameterValues(Collections.singletonMap("city", city))
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<CustomerJPA> jpaItemWriter() {
        return (items -> items.forEach(System.out::println));
    }
}
