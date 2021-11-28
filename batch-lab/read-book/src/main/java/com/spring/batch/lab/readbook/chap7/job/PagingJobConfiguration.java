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
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PagingJobConfiguration {

    public static final String JOB_NAME = "chap7_paging_job";
    public static final String STEP_NAME = "chap7_paging_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job pagingJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(pagingStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step pagingStep() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<CustomerDB, CustomerDB>chunk(10)
                .reader(pagingItemReader(null, null, null))
                .writer(pagingItemWriter())
                .build();
    }

    @Bean
    public JdbcPagingItemReader<CustomerDB> pagingItemReader(DataSource dataSource,
                                                             PagingQueryProvider queryProvider,
                                                             @Value("#{jobParameters['city']}") String city) {
        Map<String, Object> parameterValues = new HashMap<>(1);
        parameterValues.put("city", city);

        return new JdbcPagingItemReaderBuilder<CustomerDB>()
                .name("pagingItemReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider)
                .parameterValues(parameterValues)
                .pageSize(10)
                .rowMapper(new CustomerDBRowMapper())
                .build();
    }

    @Bean
    public SqlPagingQueryProviderFactoryBean pagingQueryProvider(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setSelectClause("select *");
        factoryBean.setFromClause("from CustomerDB");
        factoryBean.setWhereClause("where city =: city");
        factoryBean.setSortKey("lastName");
        factoryBean.setDataSource(dataSource);

        return factoryBean;
    }

    @Bean
    public ItemWriter<CustomerDB> pagingItemWriter() {
        return (items) -> items.forEach(System.out::println);
    }
}
