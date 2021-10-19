package com.spring.batch.lab.testfixture.test;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.ApplicationContext;

public class TestJobLauncher {
    private final ApplicationContext applicationContext;
    private final JobRepository jobRepository;
    private final JobLauncher jobLauncher;

    public TestJobLauncher(ApplicationContext applicationContext, JobRepository jobRepository, JobLauncher jobLauncher) {
        this.applicationContext = applicationContext;
        this.jobRepository = jobRepository;
        this.jobLauncher = jobLauncher;
    }

    public JobExecution launchJob(String jobName, JobParameters jobParameters) {
        Job job = applicationContext.getBean(jobName, Job.class);
        try {
            return getJobLauncherTestUtils(job).launchJob(jobParameters);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public JobExecution launchStep(String jobName, String stepName, JobParameters jobParameters) {
        Job job = applicationContext.getBean(jobName, Job.class);
        try {
            return getJobLauncherTestUtils(job).launchStep(stepName, jobParameters);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private JobLauncherTestUtils getJobLauncherTestUtils(Job job) {
        JobLauncherTestUtils jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
        jobLauncherTestUtils.setJobRepository(jobRepository);
        jobLauncherTestUtils.setJob(job);
        return jobLauncherTestUtils;
    }
}
