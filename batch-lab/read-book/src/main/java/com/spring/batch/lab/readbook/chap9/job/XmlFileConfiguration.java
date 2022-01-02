package com.spring.batch.lab.readbook.chap9.job;

import com.spring.batch.lab.readbook.chap9.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class XmlFileConfiguration {

    public static final String JOB_NAME = "chap9_xmlWriter_job";
    public static final String STEP_NAME = "chap9_xmlWriter_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job xmlFormatJob() throws Exception {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(xmlFormatStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step xmlFormatStep() throws Exception {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<Customer, Customer>chunk(10)
                .reader(xmlFormatFileReader(null))
                .writer(xmlFormatFileWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> xmlFormatFileReader(
            @Value("#{jobParameters['customerFile']}") ClassPathResource inpurtFile) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("xmlFormatFileReader")
                .resource(inpurtFile)
                .delimited()
                .names(new String[]{"firstName",
                        "middleInitial",
                        "lastName",
                        "address",
                        "city",
                        "state",
                        "zip"})
                .targetType(Customer.class)
                .build();
    }

    @Bean
    @StepScope
    public StaxEventItemWriter<Customer> xmlFormatFileWriter(
            @Value("#{jobParameters['outputFile']}") ClassPathResource outputFile) throws Exception {
        Map<String, Class> aliases = new HashMap<>();
        aliases.put("custmer", Customer.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);
        marshaller.afterPropertiesSet();

        return new StaxEventItemWriterBuilder<Customer>()
                .name("xmlFormatFileWriter")
                .resource(outputFile) // resource
                .marshaller(marshaller) // 마샬러 구현체
                .rootTagName("customers") // 루트 태그
                .build();
    }
}
