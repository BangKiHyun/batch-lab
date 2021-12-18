package com.spring.batch.lab.readbook.chap8.job;

import com.spring.batch.lab.readbook.chap8.model.Customer;
import com.spring.batch.lab.readbook.chap8.service.UpperCaseNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemProcessorAdapter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@RequiredArgsConstructor
public class ItemProcessorAdapterConfiguration {

    public static final String JOB_NAME = "chap8_ItemProcessorAdapter_job";
    public static final String STEP_NAME = "chap8_ItemProcessorAdapter_step";

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
                .reader(adapterItemReader(null))
                .processor(itemProcessorAdapter(null))
                .writer(adapterItemWriter())
                .build();
    }

    @Bean
    public ItemReader<Customer> adapterItemReader(
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
    public ItemProcessorAdapter<Customer, Customer> itemProcessorAdapter(UpperCaseNameService service) {
        ItemProcessorAdapter<Customer, Customer> adapter = new ItemProcessorAdapter<>();

        adapter.setTargetObject(service); // 호출 하려는 인스턴스
        adapter.setTargetMethod("upperCase"); // 해당 인스턴스에서 호출할 메서드

        return adapter;
    }

    @Bean
    public ItemWriter<Customer> adapterItemWriter() {
        return (items -> items.forEach(System.out::println));
    }
}
