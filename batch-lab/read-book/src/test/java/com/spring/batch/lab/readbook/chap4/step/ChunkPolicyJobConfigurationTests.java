package com.spring.batch.lab.readbook.chap4.step;

import com.spring.batch.lab.readbook.test.BatchSpringTest;
import com.spring.batch.lab.testfixture.test.TestJobLauncher;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@BatchSpringTest
class ChunkPolicyJobConfigurationTests {

    @Autowired
    private TestJobLauncher jobLauncher;

    @Test
    public void chunkStepTest() throws Exception {
        //given
        final JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();

        //when
        JobExecution execution = jobLauncher.launchJob(ChunkPolicyJobConfiguration.JOB_NAME, jobParameters);
        List<StepExecution> stepExecutions = new ArrayList<>(execution.getStepExecutions());

        //then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        assertThat(stepExecutions.get(0).getReadCount()).isEqualTo(100_000);
        assertThat(stepExecutions.get(0).getWriteCount()).isEqualTo(100_000);
    }
}
