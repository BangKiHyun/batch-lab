package com.spring.batch.lab.readbook.chap8.job;

import com.spring.batch.lab.readbook.chap8.model.Customer;
import com.spring.batch.lab.readbook.chap8.validator.UniqueLastNameValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@RequiredArgsConstructor
public class ValidationItemProcessorConfiguration {

    public static final String JOB_NAME = "chap8_validatingItemProcessor_job";
    public static final String STEP_NAME = "chap8_validatingItemProcessor_step";

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
                .reader(customerValidatingItemReader(null))
                .processor(beanValidatingItemProcessor())
                .writer(validatingItemWriter())
                .stream(customValidator())
                .build();
    }

    @Bean
    public ItemReader<Customer> customerValidatingItemReader(
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
    public BeanValidatingItemProcessor<Customer> beanValidatingItemProcessor() {
        return new BeanValidatingItemProcessor<>();
    }

    @Bean
    public ValidatingItemProcessor<Customer> customerValidatingItemProcessor() {
        return new ValidatingItemProcessor<>(customValidator());
    }

    @Bean
    public UniqueLastNameValidator customValidator() {
        UniqueLastNameValidator uniqueLastNameValidator = new UniqueLastNameValidator();
        uniqueLastNameValidator.setName("customValidator");
        return uniqueLastNameValidator;
    }

    @Bean
    public ItemWriter<Customer> validatingItemWriter() {
        return (items -> items.forEach(System.out::println));
    }
}
