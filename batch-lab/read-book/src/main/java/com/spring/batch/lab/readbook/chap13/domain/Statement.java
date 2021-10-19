package com.spring.batch.lab.readbook.chap13.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Statement {

    private final Customer customer;
    private List<Account> accounts = new ArrayList<>();

    public Statement(Customer customer, List<Account> accounts) {
        this.customer = customer;
        this.accounts.addAll(accounts);
    }

    public Statement(Customer customer) {
        this.customer = customer;
    }
}
