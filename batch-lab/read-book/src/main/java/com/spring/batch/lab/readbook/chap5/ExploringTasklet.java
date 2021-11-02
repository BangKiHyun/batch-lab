package com.spring.batch.lab.readbook.chap5;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

public class ExploringTasklet implements Tasklet {

    private JobExplorer jobExplorer;

    public ExploringTasklet(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    /***
     * JobInstance 실행 갯수 및 각 JobInstance당 얼마나 많은 실제 실행이 있었는지 확인
     */

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String jobName = chunkContext.getStepContext().getJobName();

        final List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, Integer.MAX_VALUE);

        System.out.println(String.format("There are %d job instances for the job %s",
                jobInstances.size(),
                jobName));

        System.out.println("They have had the following results");
        System.out.println("***********************************");

        for (JobInstance instance : jobInstances) {
            final List<JobExecution> jobExecutions = this.jobExplorer.getJobExecutions(instance);

            System.out.println(String.format("Instance %d had %d executions",
                    instance.getInstanceId(),
                    jobExecutions.size()));

            for (JobExecution jobExecution : jobExecutions) {
                System.out.println(String.format("\tExecution %d resulted in Exit Status %s",
                        jobExecution.getId(),
                        jobExecution.getExitStatus()));
            }
        }
        return RepeatStatus.FINISHED;
    }
}
