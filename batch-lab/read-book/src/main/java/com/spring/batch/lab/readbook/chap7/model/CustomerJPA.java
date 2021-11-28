package com.spring.batch.lab.readbook.chap7.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "customerJPA")
@Getter
@ToString
@NoArgsConstructor
public class CustomerJPA {

    @Id
    private Long id;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "middleInitial")
    private String middleInitial;

    @Column(name = "lastName")
    private String lastName;

    private String address;
    private String city;
    private String state;
    private String zipCode;

    @Builder
    public CustomerJPA(Long id, String firstName, String middleInitial, String lastName, String address, String city,
                       String state, String zipCode) {
        this.id = id;
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }
}
