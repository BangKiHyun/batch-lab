package com.spring.batch.lab.readbook.chap6.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountSummary {

    private int id;
    private String accountNumber;
    private Double currentBalance;

    @Builder
    public AccountSummary(int id, String accountNumber, Double currentBalance) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.currentBalance = currentBalance;
    }

    public void setCurrentBalance(Double currentBalance) {
        this.currentBalance = currentBalance;
    }
}
