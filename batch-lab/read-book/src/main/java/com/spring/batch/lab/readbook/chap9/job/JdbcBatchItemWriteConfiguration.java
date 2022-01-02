package com.spring.batch.lab.readbook.chap9.job;

import com.spring.batch.lab.readbook.chap9.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class JdbcBatchItemWriteConfiguration {

    public static final String JOB_NAME = "chap9_jdbcBatchWriter_job";
    public static final String STEP_NAME = "chap9_jdbcBatchWriter_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job jdbcBatchJob() throws Exception {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(jdbcBatchStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step jdbcBatchStep() throws Exception {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<Customer, Customer>chunk(10)
                .reader(jdbcCustomerReader(null))
                .writer(jdbcCustomerWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> jdbcCustomerReader(
            @Value("#{jobParameters['inputFile']}") ClassPathResource inputFile) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("jdbcCustomerReader")
                .resource(inputFile)
                .delimited()
                .names(new String[]{"firstName",
                        "middleInitial",
                        "lastName",
                        "address",
                        "city",
                        "state",
                        "zip"})
                .build();
    }

	@Bean
    @StepScope
	public JdbcBatchItemWriter<Customer> jdbcCustomerWriter(DataSource dataSource) throws Exception {
		return new JdbcBatchItemWriterBuilder<Customer>()
				.dataSource(dataSource)
				.sql("INSERT INTO CUSTOMER (first_name, " +
						"middle_initial, " +
						"last_name, " +
						"address, " +
						"city, " +
						"state, " +
						"zip) VALUES (?, ?, ?, ?, ?, ?, ?)")
				.itemPreparedStatementSetter(new CustomItemPreparedStatementSetter()) // (?) 플레이스홀더를 사용할 경우 해당 부분 추가
				.build();
	}

	@Bean
    @StepScope
	public JdbcBatchItemWriter<Customer> jdbcCustomerNamedParameterWriter(DataSource dataSource) throws Exception {
		return new JdbcBatchItemWriterBuilder<Customer>()
				.dataSource(dataSource)
				.sql("INSERT INTO CUSTOMER (first_name, " +
						"middle_initial, " +
						"last_name, " +
						"address, " +
						"city, " +
						"state, " +
						"zip) VALUES (:firstName, " +
						":middleInitial, " +
						":lastName, " +
						":address, " +
						":city, " +
						":state, " +
						":zip)")
				.beanMapped() // NamedParameter를 사용할 경우 itemPreparedStatementSetter의 참조 필요 X
				.build();
	}
}
