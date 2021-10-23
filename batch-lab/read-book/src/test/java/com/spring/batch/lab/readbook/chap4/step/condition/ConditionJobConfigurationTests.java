package com.spring.batch.lab.readbook.chap4.step.condition;

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
class ConditionJobConfigurationTests {

    @Autowired
    private TestJobLauncher jobLauncher;

    @DisplayName("조건 로직 성공 스텝 테스트")
    @Test
    public void conditionJobSuccessStepTest() throws Exception {
        //given
        final JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();

        //when
        final JobExecution execution = jobLauncher.launchJob(ConditionJobConfiguration.JOB_NAME, jobParameters);

        //then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.STOPPED);
    }

    @DisplayName("조건 로직 Stopped 상태 이후 stopAndRestart() 메서드에 지정한 스탭이 돌아가는지 확인")
    @Test
    public void conditionJobSuccessStepAndNextStepTest() throws Exception {
        //given
        final JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();

        //when
        final JobExecution firstExecution = jobLauncher.launchJob(ConditionJobConfiguration.JOB_NAME, jobParameters);
        final JobExecution secondExecution = jobLauncher.launchJob(ConditionJobConfiguration.JOB_NAME, jobParameters);

        //then`
        assertThat(firstExecution.getStatus()).isEqualTo(BatchStatus.STOPPED);
        assertThat(secondExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}