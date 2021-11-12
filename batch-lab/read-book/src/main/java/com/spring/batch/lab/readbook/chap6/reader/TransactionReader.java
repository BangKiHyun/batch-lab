package com.spring.batch.lab.readbook.chap6.reader;

import com.spring.batch.lab.readbook.chap6.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.transform.FieldSet;

@RequiredArgsConstructor
public class TransactionReader implements ItemStreamReader<Transaction> {

    private final ItemStreamReader<FieldSet> fieldSetReader;
    private int recordCount = 0;
    private int expectedRecordCount = 0;

    @Override
    public Transaction read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return process(fieldSetReader.read());
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.fieldSetReader.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        this.fieldSetReader.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        this.fieldSetReader.close();
    }

    private Transaction process(FieldSet fieldSet) {
        Transaction result = null;
        if (fieldSet != null) {
            if (fieldSet.getFieldCount() > 1) {
                result = Transaction.builder()
                        .accountNumber(fieldSet.readString(0))
                        .timestamp(fieldSet.readDate(1, "yyyy-MM-DD HH:mm:ss"))
                        .amount(fieldSet.readDouble(2))
                        .build();
                recordCount++;
            } else {
                expectedRecordCount = fieldSet.readInt(0);
            }
        }
        return result;
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution execution) {
        if (recordCount == expectedRecordCount) {
            return execution.getExitStatus();
        }
        return ExitStatus.STOPPED;
    }
}
