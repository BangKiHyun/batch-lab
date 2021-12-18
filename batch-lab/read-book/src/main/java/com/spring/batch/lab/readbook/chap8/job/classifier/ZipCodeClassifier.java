package com.spring.batch.lab.readbook.chap8.job.classifier;

import com.spring.batch.lab.readbook.chap8.model.Customer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.classify.Classifier;

public class ZipCodeClassifier implements Classifier<Customer, ItemProcessor<Customer, Customer>> {

    private ItemProcessor<Customer, Customer> oddItemProcessor;
    private ItemProcessor<Customer, Customer> evenItemProcessor;

    public ZipCodeClassifier(ItemProcessor<Customer, Customer> oddItemProcessor,
                             ItemProcessor<Customer, Customer> evenItemProcessor) {
        this.oddItemProcessor = oddItemProcessor;
        this.evenItemProcessor = evenItemProcessor;
    }

    @Override
    public ItemProcessor<Customer, Customer> classify(Customer classifiable) {
        if (Integer.parseInt(classifiable.getZip()) % 2 == 0) {
            return evenItemProcessor;
        }
        return oddItemProcessor;
    }
}
