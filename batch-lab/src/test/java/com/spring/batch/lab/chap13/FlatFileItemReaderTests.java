package com.spring.batch.lab.chap13;

import com.spring.batch.lab.chap13.batch.AccountItemProcessor;
import com.spring.batch.lab.chap13.configuration.ImportJobConfiguration;
import com.spring.batch.lab.chap13.domain.CustomerAddressUpdate;
import com.spring.batch.lab.chap13.domain.CustomerContactUpdate;
import com.spring.batch.lab.chap13.domain.CustomerNameUpdate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ImportJobConfiguration.class,
        CustomerItemValidator.class,
        AccountItemProcessor.class})
@JdbcTest
@EnableBatchProcessing
@SpringBatchTest
public class FlatFileItemReaderTests {

    @Autowired
    private FlatFileItemReader<CustomerUpdate> customerUpdateItemReader;

    public StepExecution getStepExecution() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("customerUpdateFile", "classpath:customerUpdateFIle.csv")
                .toJobParameters();

        return MetaDataInstanceFactory.createStepExecution(jobParameters);
    }

    @Test
    public void testTypeConversion() throws Exception {
        this.customerUpdateItemReader.open(new ExecutionContext());

        assertTrue(this.customerUpdateItemReader.read() instanceof CustomerAddressUpdate);
        assertTrue(this.customerUpdateItemReader.read() instanceof CustomerContactUpdate);
        assertTrue(this.customerUpdateItemReader.read() instanceof CustomerNameUpdate);
    }
}
