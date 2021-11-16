package com.spring.batch.lab.readbook.chap7.job;

import com.spring.batch.lab.readbook.test.BatchSpringTest;
import com.spring.batch.lab.testfixture.test.TestJobLauncher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@BatchSpringTest
class ItemReaderJobConfigurationTests {

    @Autowired
    private TestJobLauncher jobLauncher;

    @DisplayName("고정된 길이의 텍스트 파일 읽기")
    @Test
    public void fixedLengthCustomerItemReaderTest() throws Exception {
        //given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("customerFile", "input/customer.txt")
                .toJobParameters();

        //when
        final JobExecution execution = jobLauncher.launchJob(ItemReaderJobConfiguration.JOB_NAME, jobParameters);

        //then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

}