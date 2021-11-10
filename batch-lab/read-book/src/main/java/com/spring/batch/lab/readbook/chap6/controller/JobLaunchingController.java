package com.spring.batch.lab.readbook.chap6.controller;

import com.spring.batch.lab.readbook.chap6.controller.dto.request.JobLauncherRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JobLaunchingController {

    private final JobLauncher jobLauncher;
    private final ApplicationContext context;
    private final JobExplorer jobExplorer;

    @PostMapping("/job/run")
    public ExitStatus runJob(@RequestBody JobLauncherRequest request) throws Exception {
        Job job = this.context.getBean(request.getName(), Job.class);
        final JobParameters jobParameters = new JobParametersBuilder(request.getJobParameters(), this.jobExplorer)
                .getNextJobParameters(job) // 파라미터 증가시키기 위한 메서드 (RunIdIncrementer 활성화)
                .toJobParameters();

        return this.jobLauncher.run(job, jobParameters)
                .getExitStatus();
    }
}
