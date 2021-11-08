package com.spring.batch.lab.readbook.chap6.controller;

import com.spring.batch.lab.readbook.chap6.controller.dto.request.JobLauncherRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
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

    @PostMapping("/job/run")
    public ExitStatus runJob(@RequestBody JobLauncherRequest request) throws Exception {
        Job job = this.context.getBean(request.getName(), Job.class);
        return this.jobLauncher.run(job, request.getJobParameters())
                .getExitStatus();
    }
}
