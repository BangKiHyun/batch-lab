package com.spring.batch.lab.chap04;

import com.spring.batch.lab.BatchLabApplication;
import com.spring.batch.lab.config.BatchConfiguration;
import com.spring.batch.lab.launcer.TestJobLauncher;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Import(BatchConfiguration.class)
@SpringBootTest(classes = BatchLabApplication.class)
class HelloWorldJobConfigurationTest {

    @Autowired
    private TestJobLauncher jobLauncher;

    @Test
    void helloWorldJobTest() {
        //given
        JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();

        //when
        JobExecution execution = jobLauncher.launchJob(HelloWorldJobConfiguration.JOB_NAME, jobParameters);

        //then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }
}