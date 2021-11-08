package com.spring.batch.lab.readbook.chap6.controller.dto.request;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import java.util.Properties;

@Setter
@NoArgsConstructor
public class JobLauncherRequest {

    private String name;
    private Properties jobParameters;

    public JobLauncherRequest(String name, Properties jobParameters) {
        this.name = name;
        this.jobParameters = jobParameters;
    }

    public String getName() {
        return this.name;
    }

    public Properties getJobParamsProperties() {
        return this.jobParameters;
    }

    public JobParameters getJobParameters() {
        Properties properties = new Properties();
        properties.putAll(this.jobParameters);
        return new JobParametersBuilder(properties)
                .toJobParameters();
    }
}
