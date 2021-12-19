package com.spring.batch.lab.readbook.chap6.step;

import com.spring.batch.lab.readbook.chap6.model.Transaction;
import com.spring.batch.lab.readbook.chap6.reader.TransactionReader;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class ImportTransactionFileStep {

    public static final String TRANSACTION_STEP_NAME = "chap6_transaction_step";
    public static final String FILE_STEP_NAME = "chap6_file_step";

    private static final String INSERT_TRANSACTION_QUERY = "insert into transaction " +
            "(account_summary_id, timestamp, amount) " +
            "values ((select id from account_summary " +
            "where account_number = :accountNumber), " +
            ":timestamp, :amount)";

    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = "import_transaction_file_step")
    public Step importTransactionFileStep() {
        return this.stepBuilderFactory.get(TRANSACTION_STEP_NAME)
                .<Transaction, Transaction>chunk(100)
                .reader(transactionReader())
                .writer(transactionWriter(null))
                .allowStartIfComplete(true)
                .listener(transactionReader())
                .build();
    }

    @Bean
    @StepScope
    public TransactionReader transactionReader() {
        return new TransactionReader(fileItemReader(null));
    }

    @Bean
    @StepScope
    public FlatFileItemReader<FieldSet> fileItemReader(
            @Value("#{jobParameters['transactionFile']}") Resource inputFile) {
        return new FlatFileItemReaderBuilder<FieldSet>()
                .name(FILE_STEP_NAME)
                .resource(inputFile)
                .lineTokenizer(new DelimitedLineTokenizer())
                .fieldSetMapper(new PassThroughFieldSetMapper())
                .build();
    }

    // 값을 DB에 저장하는 역할
    @Bean
    public JdbcBatchItemWriter<Transaction> transactionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .itemSqlParameterSourceProvider(
                        new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql(INSERT_TRANSACTION_QUERY)
                .dataSource(dataSource)
                .build();
    }
}
