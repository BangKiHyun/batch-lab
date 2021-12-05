package com.spring.batch.lab.readbook.chap7.job;

import com.spring.batch.lab.readbook.chap7.mapper.CustomerJpaRowMapper;
import com.spring.batch.lab.readbook.chap7.model.CustomerJPA;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.StoredProcedureItemReader;
import org.springframework.batch.item.database.builder.StoredProcedureItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameter;

import javax.sql.DataSource;
import java.sql.Types;

@Configuration
@RequiredArgsConstructor
public class StoredProcedureJobConfiguration {

    public static final String JOb_NAME = "chap7_SP_job";
    public static final String STEP_NAME = "chap7_SP_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOb_NAME)
    public Job storedProcedureJob() {
        return this.jobBuilderFactory.get(JOb_NAME)
                .start(storedProcedureStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step storedProcedureStep() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<CustomerJPA, CustomerJPA>chunk(10)
                .reader(storedProcedureItemReader(null, null))
                .writer(storedProcedureItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public StoredProcedureItemReader<CustomerJPA> storedProcedureItemReader(
            DataSource dataSource,
            @Value("#{jobParameters['city']}") String city) {
        return new StoredProcedureItemReaderBuilder<CustomerJPA>()
                .name("storedProcedureItemReader")
                .dataSource(dataSource)
                .procedureName("customer_list")
                .parameters(new SqlParameter[]{
                        new SqlParameter("cityOption", Types.VARBINARY)})
                .preparedStatementSetter(
                        new ArgumentPreparedStatementSetter(new Object[] {city}))
                .rowMapper(new CustomerJpaRowMapper())
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<CustomerJPA> storedProcedureItemWriter() {
        return (items -> items.forEach(System.out::println));
    }
}
