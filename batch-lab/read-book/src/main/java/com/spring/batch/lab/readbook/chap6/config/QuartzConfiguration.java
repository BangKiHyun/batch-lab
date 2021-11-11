package com.spring.batch.lab.readbook.chap6.config;

import com.spring.batch.lab.readbook.chap6.scheduler.BatchScheduledJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfiguration {

    private static final int INTERVAL_SECONDS = 5;
    private static final int REPEAT_COUNT = 4;

    @Bean
    public JobDetail quartzJobDetails() {
        return JobBuilder.newJob(BatchScheduledJob.class)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger jobTrigger() {
        final SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(INTERVAL_SECONDS) // job 실행 간격
                .withRepeatCount(REPEAT_COUNT); // job 반복 횟수

        return TriggerBuilder.newTrigger()
                .forJob(quartzJobDetails())
                .withSchedule(scheduleBuilder)
                .build();
    }
}
