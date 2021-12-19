package com.spring.batch.lab.readbook.chap9.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Customer {

    private static final long serialVersionUID = 1L;

    private long id;
    private String firstname;
    private String middleInitial;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String zip;

    public Customer(String firstname, String middleInitial, String lastName,
                    String address, String city, String state, String zip) {
        this.firstname = firstname;
        this.middleInitial = middleInitial;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }
}
