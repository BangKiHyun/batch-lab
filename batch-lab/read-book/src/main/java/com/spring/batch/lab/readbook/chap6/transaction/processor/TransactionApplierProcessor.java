package com.spring.batch.lab.readbook.chap6.transaction.processor;

import com.spring.batch.lab.readbook.chap6.model.AccountSummary;
import com.spring.batch.lab.readbook.chap6.model.Transaction;
import com.spring.batch.lab.readbook.chap6.transaction.TransactionDao;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

@RequiredArgsConstructor
public class TransactionApplierProcessor implements ItemProcessor<AccountSummary, AccountSummary> {

    private final TransactionDao transactionDao;

    @Override
    public AccountSummary process(AccountSummary summary) throws Exception {
        List<Transaction> transactions = transactionDao
                .getTransactionsByAccountNumber(summary.getAccountNumber());

        for (Transaction transaction : transactions) {
            summary.setCurrentBalance(summary.getCurrentBalance() + transaction.getAmount());
        }
        return summary;
    }
}
