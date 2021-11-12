package com.spring.batch.lab.readbook.chap6.transaction;


import com.spring.batch.lab.readbook.chap6.model.Transaction;

import java.util.List;

public interface TransactionDao {

    List<Transaction> getTransactionsByAccountNumber(String accountNumber);
}
