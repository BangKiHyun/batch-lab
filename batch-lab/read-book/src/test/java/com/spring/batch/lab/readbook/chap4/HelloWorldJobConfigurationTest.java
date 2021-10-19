package com.spring.batch.lab.readbook.chap4;

import com.spring.batch.lab.readbook.test.BatchSpringTest;
import com.spring.batch.lab.testfixture.test.TestJobLauncher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@BatchSpringTest
class HelloWorldJobConfigurationTest {

    @Autowired
    private TestJobLauncher jobLauncher;

    @DisplayName("DefaultJobParametersValidator 설정한 필수 및 옵션 파라미터를 모두 넣어줌")
    @Test
    public void validateAllTest() throws Exception {
        //given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileName", "test.csv")
                .addString("name", "bang")
                .addDate("currentDate", new Date())
                .toJobParameters();

        //when
        final JobExecution execution = jobLauncher.launchJob(HelloWorldJobConfiguration.JOB_NAME, jobParameters);

        //then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }

    @DisplayName("DefaultJobParametersValidator에 설정한 필수 파라미터만 넣어줌")
    @Test
    public void validateRequiredTest() throws Exception {
        //given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileName", "test.csv")
                .toJobParameters();

        //when
        final JobExecution execution = jobLauncher.launchJob(HelloWorldJobConfiguration.JOB_NAME, jobParameters);

        //then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }

    @DisplayName("DefaultJobParametersValidator에 설정한 필수 파라미터 및 일부 옵션 파라미터만 넣어줌")
    @Test
    public void validateRequiredAndOptionTest() throws Exception {
        //given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileName", "test.csv")
                .addDate("currentDate", new Date())
                .toJobParameters();

        //when
        final JobExecution execution = jobLauncher.launchJob(HelloWorldJobConfiguration.JOB_NAME, jobParameters);

        //then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }

    @DisplayName("DefaultJobParametersValidator에 설정한 필수 파라미터를 넣어주지 않음")
    @Test
    public void validateRequiredNotContainsTest() throws Exception {
        //given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("name", "bang")
                .toJobParameters();

        //then
        assertThatThrownBy(() -> jobLauncher.launchJob(HelloWorldJobConfiguration.JOB_NAME, jobParameters))
                .isInstanceOf(RuntimeException.class);
    }

    @DisplayName("DefaultJobParametersValidator에 설정하지 않은 파라미터를 넣어줌")
    @Test
    public void validateNotSettingParameterTest() throws Exception {
        //given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileName", "test.csv")
                .addString("not contains", "not contains")
                .addDate("currentDate", new Date())
                .toJobParameters();

        //then
        assertThatThrownBy(() -> jobLauncher.launchJob(HelloWorldJobConfiguration.JOB_NAME, jobParameters))
                .isInstanceOf(RuntimeException.class);
    }
}