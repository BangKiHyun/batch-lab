package com.spring.batch.lab.readbook.chap13.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@XmlRootElement(name = "transaction")
public class Transaction {

    private long transactionId;

    private long accountId;

    private String description;

    private BigDecimal credit;

    private BigDecimal debit;

    private Date timestamp;

    public Transaction(long transactionId, long accountId, String description, BigDecimal credit, BigDecimal debit, Date timestamp) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.description = description;
        this.credit = credit;
        this.debit = debit;
        this.timestamp = timestamp;
    }

    public BigDecimal getTransactionAmount() {
        if(credit != null) {
            if(debit != null) {
                return credit.add(debit);
            }
            else {
                return credit;
            }
        }
        else if(debit != null) {
            return debit;
        }
        else {
            return new BigDecimal(0);
        }
    }
}
