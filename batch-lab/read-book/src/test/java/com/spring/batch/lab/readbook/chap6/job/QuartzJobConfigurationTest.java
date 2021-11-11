package com.spring.batch.lab.readbook.chap6.job;

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
class QuartzJobConfigurationTest {

    @Autowired
    private TestJobLauncher jobLauncher;

    @DisplayName("쿼츠 잡 스케줄링 테스트")
    @Test
    public void quartzJobTest() throws Exception {
        //given
        final JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();

        //when
        final JobExecution execution = jobLauncher.launchJob(QuartzJobConfiguration.JOB_NAME, jobParameters);

        //then
        assertThat(execution.getExitStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}