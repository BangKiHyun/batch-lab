package com.spring.batch.lab.readbook.chap9.job;

import com.spring.batch.lab.readbook.chap9.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@RequiredArgsConstructor
public class FormattedTextFileConfiguration {

    public static final String JOB_NAME = "chap9_flatWriter_job";
    public static final String STEP_NAME = "chap9_flatWriter_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job job() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(formattedTextFileStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step formattedTextFileStep() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<Customer, Customer>chunk(10)
                .reader(flatFileReader(null))
                .writer(flatItemWriter(null))
                .build();
    }

    @Bean(name = STEP_NAME + "FlatFileReader")
    @StepScope
    public FlatFileItemReader<Customer> flatFileReader(
            @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name(STEP_NAME + "FlatFileReader")
                .resource(inputFile)
                .delimited()
                .names(new String[]{"firstName",
                        "middleInitial",
                        "lastName",
                        "address",
                        "city",
                        "state",
                        "zip"})
                .build();
    }

    @Bean(name = STEP_NAME + "FlatFileWriter")
    @StepScope
    public FlatFileItemWriter<Customer> flatItemWriter(
            @Value("#{jobParameters['outputFile']}") ClassPathResource outputFile) {
        return new FlatFileItemWriterBuilder<Customer>()
                .name(STEP_NAME + "FlatFileWriter")
                .resource(outputFile)
                .formatted()
                .format("%s %s lives at %s %s in %s %s.") // 포매팅 지정
                .names(new String[]{"firstName",
                        "middleInitial",
                        "lastName",
                        "address",
                        "city",
                        "state",
                        "zip"})
                .build();
    }

    @Bean(name = STEP_NAME + "Delimited")
    @StepScope
    public FlatFileItemWriter<Customer> delimitedItemWriter(
            @Value("#{jobParameters['outputFile']}") ClassPathResource outputFile) {
        return new FlatFileItemWriterBuilder<Customer>()
                .name(STEP_NAME + "Delimited")
                .resource(outputFile)
                .delimited() // default: 콤마(,)
                .delimiter(";") // 세미콜론(;)으로 변경
                .names(new String[]{"firstName",
                        "middleInitial",
                        "lastName",
                        "address",
                        "city",
                        "state",
                        "zip"})
                .build();
    }

}
