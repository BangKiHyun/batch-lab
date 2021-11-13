package com.spring.batch.lab.readbook.chap7.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class Customer {

    private String middleInitial;
    private String lastName;
    private String addressNumber;
    private String street;
    private String city;
    private String state;
    private String zipCode;

    @Builder
    public Customer(String middleInitial, String lastName, String addressNumber,
                    String street, String city, String state, String zipCode) {
        this.middleInitial = middleInitial;
        this.lastName = lastName;
        this.addressNumber = addressNumber;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }
}
