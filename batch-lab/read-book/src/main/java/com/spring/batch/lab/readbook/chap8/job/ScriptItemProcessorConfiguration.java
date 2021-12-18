package com.spring.batch.lab.readbook.chap8.job;

import com.spring.batch.lab.readbook.chap8.job.classifier.ZipCodeClassifier;
import com.spring.batch.lab.readbook.chap8.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.ScriptItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class ScriptItemProcessorConfiguration {

    public static final String JOB_NAME = "chap8_scriptItemProcessor_job";
    public static final String STEP_NAME = "chap8_scriptItemProcessor_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job job() throws Exception {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(validatingStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step validatingStep() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<Customer, Customer>chunk(5)
                .reader(scriptItemReader(null))
                .processor(upperCaseScriptItemProcessor(null))
                .writer(scriptItemWriter())
                .build();
    }

    @Bean
    public ItemReader<Customer> scriptItemReader(
            @Value("#{jobParameters['customerFile']}") Resource inputFile) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerValidatingItemReader")
                .delimited()
                .names(new String[]{"firstName",
                        "middleInitial",
                        "lastName",
                        "address",
                        "city",
                        "state",
                        "zip"})
                .targetType(Customer.class)
                .resource(inputFile)
                .build();
    }

    @Bean
    @StepScope
    public ScriptItemProcessor<Customer, Customer> upperCaseScriptItemProcessor(
            @Value("#{jobParametersp['script']}") Resource script) {
        ScriptItemProcessor<Customer, Customer> itemProcessor = new ScriptItemProcessor<>();

        itemProcessor.setScript(script);

        return itemProcessor;
    }

    @Bean
    @StepScope
    public ScriptItemProcessor<Customer, Customer> lowerCaseScriptItemProcessor(
            @Value("#{jobParametersp['script']}") Resource script) {
        ScriptItemProcessor<Customer, Customer> itemProcessor = new ScriptItemProcessor<>();

        itemProcessor.setScript(script);

        return itemProcessor;
    }

    @Bean
    public CompositeItemProcessor<Customer, Customer> compositeItemProcessor() {
        CompositeItemProcessor<Customer, Customer> itemProcessor = new CompositeItemProcessor<>();

        itemProcessor.setDelegates(Arrays.asList(
                upperCaseScriptItemProcessor(null),
                lowerCaseScriptItemProcessor(null)
        ));

        return itemProcessor;
    }

    @Bean
    public Classifier classifier() {
        return new ZipCodeClassifier(upperCaseScriptItemProcessor(null),
                lowerCaseScriptItemProcessor(null));
    }

    public ClassifierCompositeItemProcessor<Customer, Customer> classifierCompositeItemProcessor() {
        ClassifierCompositeItemProcessor<Customer, Customer> itemProcessor
                = new ClassifierCompositeItemProcessor<>();

        itemProcessor.setClassifier(classifier());

        return itemProcessor;
    }

    @Bean
    public ItemWriter<Customer> scriptItemWriter() {
        return (items -> items.forEach(System.out::println));
    }
}
