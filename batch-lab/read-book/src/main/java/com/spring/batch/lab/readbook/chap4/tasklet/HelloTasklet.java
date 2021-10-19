package com.spring.batch.lab.readbook.chap4.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

public class HelloTasklet implements Tasklet {

    private static final String HELLO_WORLD = "Hello, %s";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String name =
                (String) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("name");

        // job context
        final ExecutionContext jobContext =
                chunkContext.getStepContext()
                        .getStepExecution()
                        .getJobExecution()
                        .getExecutionContext();

        // step context
        final ExecutionContext stepContext =
                chunkContext.getStepContext()
                        .getStepExecution()
                        .getExecutionContext();



        jobContext.put("user.name", "name");

        System.out.println(String.format(HELLO_WORLD, name));
        return RepeatStatus.FINISHED;
    }
}
