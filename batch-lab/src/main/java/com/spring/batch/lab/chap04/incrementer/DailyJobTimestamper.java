package com.spring.batch.lab.chap04.incrementer;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DailyJobTimestamper implements JobParametersIncrementer {

    private static final String TIMESTAMP_KEY = "currentDate";

    @Override
    public JobParameters getNext(JobParameters parameters) {
        return new JobParametersBuilder(parameters)
                .addDate(TIMESTAMP_KEY, new Date())
                .toJobParameters();
    }
}
