package com.spring.batch.lab.readbook.chap7.job;

import com.spring.batch.lab.readbook.chap7.mapper.CustomerDBRowMapper;
import com.spring.batch.lab.readbook.chap7.model.CustomerDB;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class CursorJobConfiguration {

    public static final String JOB_NAME = "chap7_cursor_job";
    public static final String STEP_NAME = "chap7_cursor_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job cursorJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(cursorStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step cursorStep() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<CustomerDB, CustomerDB>chunk(10)
                .reader(cursorItemReader(null))
                .writer(cursorItemWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<CustomerDB> cursorItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<CustomerDB>()
                .name("cursorItemReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM customerDB")
                .rowMapper(new CustomerDBRowMapper())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<CustomerDB> cursorItemReaderWithParameter(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<CustomerDB>()
                .name("cursorItemReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM customerDB WHERE city = ?")
                .rowMapper(new CustomerDBRowMapper())
                .preparedStatementSetter(citySetter(null))
                .build();
    }

    @Bean
    @StepScope
    public ArgumentPreparedStatementSetter citySetter(
            @Value("#{jobParameters['city']}") String city) {
        return new ArgumentPreparedStatementSetter(new Object[]{city});
    }

    @Bean
    public ItemWriter<CustomerDB> cursorItemWriter() {
        return (items) -> items.forEach(System.out::println);
    }
}
