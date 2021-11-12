package com.spring.batch.lab.readbook.chap6.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
public class Transaction {

    private String accountNumber;
    private Date timestamp;
    private double amount;

    @Builder
    public Transaction(String accountNumber, Date timestamp, double amount) {
        this.accountNumber = accountNumber;
        this.timestamp = timestamp;
        this.amount = amount;
    }
}
