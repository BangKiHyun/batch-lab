package com.spring.batch.lab.readbook.chap4.step;

import com.spring.batch.lab.readbook.test.BatchSpringTest;
import com.spring.batch.lab.testfixture.test.TestJobLauncher;
import org.junit.jupiter.api.DisplayName;
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

    @DisplayName("청크 스텝 정적 커밋 개수 테스트")
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

        assertThat(stepExecutions.get(0).getCommitCount()).isEqualTo(101);
    }
}
