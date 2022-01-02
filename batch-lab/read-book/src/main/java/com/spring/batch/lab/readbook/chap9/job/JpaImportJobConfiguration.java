package com.spring.batch.lab.readbook.chap9.job;

import com.spring.batch.lab.readbook.chap7.model.CustomerJPA;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;

@Configuration
@RequiredArgsConstructor
public class JpaImportJobConfiguration {

    public static final String JOB_NAME = "chap9_jpa_job";
    public static final String STEP_NAME = "chap9_jpa_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job jpaJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(jpaStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step jpaStep() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<CustomerJPA, CustomerJPA>chunk(10)
                .reader(jpaReader(null))
                .writer(jpaWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CustomerJPA> jpaReader(
            @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
        return new FlatFileItemReaderBuilder<CustomerJPA>()
                .name(STEP_NAME + "FlatFileReader")
                .resource(inputFile)
                .delimited()
                .names(new String[]{"firstName",
                        "middleInitial",
                        "lastName",
                        "address",
                        "city",
                        "state",
                        "zip"})
                .targetType(CustomerJPA.class)
                .build();
    }

    @Bean
    @StepScope
    public JpaItemWriter<CustomerJPA> jpaWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<CustomerJPA> itemWriter = new JpaItemWriter<>();
        itemWriter.setEntityManagerFactory(entityManagerFactory);
        return itemWriter;
    }
}
