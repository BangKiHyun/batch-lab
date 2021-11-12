package com.spring.batch.lab.readbook.chap6.job;

import com.spring.batch.lab.readbook.chap6.step.ApplyTransactionStep;
import com.spring.batch.lab.readbook.chap6.step.ImportTransactionFileStep;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TransactionJobConfiguration {

    public static final String JOB_NAME = "chap6_transaction_job";

    private final JobBuilderFactory jobBuilderFactory;
    private final ApplyTransactionStep applyTransactionStep;
    private final ImportTransactionFileStep importTransactionFileStep;

    @Bean(name = JOB_NAME)
    public Job transactionJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(importTransactionFileStep.importTransactionFileStep())
                .on("STOPPED").stopAndRestart(importTransactionFileStep.importTransactionFileStep())
                .from(importTransactionFileStep.importTransactionFileStep()).on("*").to(applyTransactionStep.applyTransactionStep())
                .from(applyTransactionStep.applyTransactionStep()).next(applyTransactionStep.generateAccountSummaryStep())
                .end()
                .build();
    }
}
