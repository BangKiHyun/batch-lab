package com.spring.batch.lab.readbook.chap7.job;

import com.spring.batch.lab.readbook.chap7.mapper.CustomerFieldSetMapper;
import com.spring.batch.lab.readbook.chap7.model.AddressCustomer;
import com.spring.batch.lab.readbook.chap7.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@RequiredArgsConstructor
public class ItemReaderJobConfiguration {

    public static final String JOB_NAME = "chap7_item_reader_job";
    public static final String STEP_NAME = "-chap7_item_reader_step";

    private final JobBuilderFactory jobBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job job() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(copyFileStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step copyFileStep() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<Customer, Customer>chunk(10)
                .reader(fixedLengthCustomerItemReader(null))
                .writer(itemWriter())
                .build();
    }

    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = "fixed_length" + STEP_NAME)
    @StepScope
    public FlatFileItemReader<Customer> fixedLengthCustomerItemReader(
            @Value("#{jobParameters['customerFile']}") Resource inputFile) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("fixed_length" + STEP_NAME)
                .resource(inputFile)
                .fixedLength()
                .columns(new Range[]{new Range(1, 11), new Range(12, 12), new Range(13, 22),
                        new Range(23, 26), new Range(27, 46), new Range(47, 62), new Range(63, 64),
                        new Range(65, 69)})
                .names("firstName", "middleInitial", "lastName",
                        "addressNumber", "street", "city", "state", "zipCode")
                .targetType(Customer.class)
                .build();
    }

    @Bean(name = "delimited" + STEP_NAME)
    @StepScope
    public FlatFileItemReader<Customer> delimitedCustomerItemReader(
            @Value("#{jobParameters['customerFile']}") Resource inputFile) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("delimited" + STEP_NAME)
                .delimited()
                .names("firstName",
                        "middleInitial",
                        "lastName",
                        "addressNumber",
                        "street",
                        "city",
                        "state",
                        "zipCode")
                .targetType(Customer.class)
                .resource(inputFile)
                .build();
    }

    @Bean(name = "mapper" + STEP_NAME)
    @StepScope
    public FlatFileItemReader<AddressCustomer> mapperCustomerItemReader(
            @Value("#{jobParameters['customerFile']}") Resource inputFile) {
        return new FlatFileItemReaderBuilder<AddressCustomer>()
                .name("mapper" + STEP_NAME)
                .delimited()
                .names("firstName",
                        "middleInitial",
                        "lastName",
                        "addressNumber",
                        "street",
                        "city",
                        "state",
                        "zipCode")
                .fieldSetMapper(new CustomerFieldSetMapper())
                .resource(inputFile)
                .build();
    }

    @Bean(name = "customer_item_writer")
    public ItemWriter<Customer> itemWriter() {
        return (items -> items.forEach(System.out::println));
    }
}
