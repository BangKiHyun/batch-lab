package com.spring.batch.lab.readbook.chap6.step;


import com.spring.batch.lab.readbook.chap6.model.AccountSummary;
import com.spring.batch.lab.readbook.chap6.transaction.TransactionDao;
import com.spring.batch.lab.readbook.chap6.transaction.processor.TransactionApplierProcessor;
import com.spring.batch.lab.readbook.chap6.transaction.support.TransactionDaoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class ApplyTransactionStep {

    public static final String ACCOUNT_SUMMARY_STEP_NAME = "chap6_account_summary_reader_step";
    public static final String APPLY_TRANSACTION_STEP_NAME = "chap6_apply_transaction_step";

    private static final String ACCOUNT_SUMMARY_QUERY = "select account_number, current_balance " +
            "from account_summary a " +
            "where a.id in (" +
            "select distinct t.account_summary_id " +
            "from transaction t) " +
            "order by a.account_number";
    private static final String ACCOUNT_SUMMARY_UPDATE_QUERY = "update account_summary " +
            "set current_balance = :currentBalance " +
            "where account_number = accountNumber";

    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = "apply_transaction_step")
    public Step applyTransactionStep() {
        return this.stepBuilderFactory.get(APPLY_TRANSACTION_STEP_NAME)
                .<AccountSummary, AccountSummary>chunk(100)
                .reader(accountSummaryReader(null))
                .processor(transactionApplierProcessor())
                .writer(accountSummaryWriter(null))
                .build();
    }

    /***
     * AccountSummary 레코드 가져오기
     */
    @Bean
    @StepScope
    public JdbcCursorItemReader<AccountSummary> accountSummaryReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<AccountSummary>()
                .name(ACCOUNT_SUMMARY_STEP_NAME)
                .dataSource(dataSource)
                .sql(ACCOUNT_SUMMARY_QUERY)
                .rowMapper((resultSet, rowNumber) -> AccountSummary.builder()
                        .accountNumber(resultSet.getString("account_number"))
                        .currentBalance(resultSet.getDouble("current_balance"))
                        .build())
                .build();
    }

    @Bean
    public TransactionDao transactionDao(DataSource dataSource) {
        return new TransactionDaoSupport(dataSource);
    }

    @Bean
    public TransactionApplierProcessor transactionApplierProcessor() {
        return new TransactionApplierProcessor(transactionDao(null));
    }

    @Bean
    public JdbcBatchItemWriter<AccountSummary> accountSummaryWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<AccountSummary>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(
                        new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql(ACCOUNT_SUMMARY_UPDATE_QUERY)
                .build();
    }

    @Bean
    public Step generateAccountSummaryStep() {
        return this.stepBuilderFactory.get("generateAccountSummaryStep")
                .<AccountSummary, AccountSummary>chunk(100)
                .reader(accountSummaryReader(null))
                .writer(accountSummaryFileWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<AccountSummary> accountSummaryFileWriter(
            @Value("#{jobParameters['summaryFile']}") Resource summaryFile) {
        DelimitedLineAggregator<AccountSummary> lineAggregator = new DelimitedLineAggregator<>();
        BeanWrapperFieldExtractor<AccountSummary> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"accountNumber", "currentBalance"});
        fieldExtractor.afterPropertiesSet();
        lineAggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<AccountSummary>()
                .name("accountSummaryFileWriter")
                .resource(summaryFile)
                .lineAggregator(lineAggregator)
                .build();
    }
}
