package com.spring.batch.lab.readbook.chap7.mapper;

import org.springframework.batch.item.file.transform.DefaultFieldSetFactory;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.FieldSetFactory;
import org.springframework.batch.item.file.transform.LineTokenizer;

import java.util.ArrayList;

public class CustomerFileLineTokenizer implements LineTokenizer {

    private static final String DELIMITER = ",";
    private final String[] names = new String[]{"firstName",
            "middleInitial",
            "lastName",
            "address",
            "city",
            "state",
            "zipCode"};

    private FieldSetFactory fieldSetFactory = new DefaultFieldSetFactory();

    @Override
    public FieldSet tokenize(String record) {
        String[] fields = record.split(DELIMITER);
        final ArrayList<String> parsedFields = new ArrayList<>();
        for (int idx = 0; idx < fields.length; idx++) {
            if (idx == 4) {
                parsedFields.set(idx - 1, parsedFields.get(idx - 1) + " " + fields[idx]);
            } else {
                parsedFields.add(fields[idx]);
            }
        }
        return fieldSetFactory.create(parsedFields.toArray(new String[0]), names);
    }
}
