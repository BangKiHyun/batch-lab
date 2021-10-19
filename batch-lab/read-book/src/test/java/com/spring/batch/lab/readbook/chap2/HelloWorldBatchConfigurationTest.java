package com.spring.batch.lab.readbook.chap2;

import com.spring.batch.lab.readbook.config.BatchConfiguration;
import com.spring.batch.lab.readbook.test.BatchSpringTest;
import com.spring.batch.lab.testfixture.test.TestJobLauncher;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@BatchSpringTest
class HelloWorldBatchConfigurationTest {

    @Autowired
    private TestJobLauncher jobLauncher;

    @Test
    void helloWorldJobTest() {
        //given
        JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();

        //when
        JobExecution execution = jobLauncher.launchJob(HelloWorldBatchConfiguration.JOB_NAME, jobParameters);

        //then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }

}