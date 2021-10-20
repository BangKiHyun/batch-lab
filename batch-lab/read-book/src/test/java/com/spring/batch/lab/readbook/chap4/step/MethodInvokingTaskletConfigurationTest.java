package com.spring.batch.lab.readbook.chap4.step;

import com.spring.batch.lab.readbook.test.BatchSpringTest;
import com.spring.batch.lab.testfixture.test.TestJobLauncher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@BatchSpringTest
class MethodInvokingTaskletConfigurationTest {

    @Autowired
    private TestJobLauncher jobLauncher;

    @DisplayName("MethodInvokingTaskletAdapter을 사용한 Tasklet 구현")
    @Test
    public void methodInvokingTaskletTest() throws Exception {
        //given
        final JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();

        //when
        final JobExecution execution = jobLauncher.launchJob(MethodInvokingTaskletConfiguration.JOB_NAME, jobParameters);

        //then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }

}