package com.spring.batch.lab.readbook.chap6.transaction.support;

import com.spring.batch.lab.readbook.chap6.model.Transaction;
import com.spring.batch.lab.readbook.chap6.transaction.TransactionDao;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class TransactionDaoSupport extends JdbcTemplate implements TransactionDao {

    public TransactionDaoSupport(DataSource dataSource) {
        super(dataSource);
    }

    @SuppressWarnings("unchecked")
    public List<Transaction> getTransactionsByAccountNumber(String accountNumber) {
        return query(
                "select t.id, t.timestamp, t.amouont " +
                        "from transaction t tinner join account_summary a on " +
                        "a.id = t.account_summary_id " +
                        "where a.account_number = ?",
                new Object[]{accountNumber},
                (rs, rowNum) -> Transaction.builder()
                        .amount(rs.getDouble("amount"))
                        .timestamp(rs.getDate("timestamp"))
                        .build()
        );
    }
}
