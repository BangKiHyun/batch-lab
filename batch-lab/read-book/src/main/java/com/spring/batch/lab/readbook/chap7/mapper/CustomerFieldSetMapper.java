package com.spring.batch.lab.readbook.chap7.mapper;

import com.spring.batch.lab.readbook.chap7.model.AddressCustomer;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class CustomerFieldSetMapper implements FieldSetMapper<AddressCustomer> {

    @Override
    public AddressCustomer mapFieldSet(FieldSet fieldSet) throws BindException {
        return AddressCustomer.builder()
                .address(fieldSet.readString("addressNumber") +
                        fieldSet.readString("street"))
                .city(fieldSet.readString("city"))
                .firstName(fieldSet.readString("firstName"))
                .middleInitial(fieldSet.readString("middleInitial"))
                .lastName(fieldSet.readString("lastName"))
                .state(fieldSet.readString("state"))
                .zipCode(fieldSet.readString("zipCode"))
                .build();
    }
}
