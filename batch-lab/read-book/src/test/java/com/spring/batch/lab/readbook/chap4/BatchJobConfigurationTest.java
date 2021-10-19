package com.spring.batch.lab.readbook.chap4;

import com.spring.batch.lab.readbook.chap4.config.BatchJobConfiguration;
import com.spring.batch.lab.readbook.test.BatchSpringTest;
import com.spring.batch.lab.testfixture.test.TestJobLauncher;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@BatchSpringTest
class BatchJobConfigurationTest {

    @Autowired
    private TestJobLauncher jobLauncher;

    @Test
    public void jobTest() {
        //given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("name", "bang")
                .toJobParameters();

        //when
        final JobExecution jobExecution = jobLauncher.launchJob(BatchJobConfiguration.JOB_NAME, jobParameters);
        final String name = jobExecution
                .getJobParameters()
                .getString("name");

        //then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(name).isEqualTo("bang");
    }
}