package com.spring.batch.lab.readbook.chap13;

public class CustomerUpdate {

    private final long customerId;

    public CustomerUpdate(long customerId) {
        this.customerId = customerId;
    }

    public long getCustomerId() {
        return customerId;
    }

    @Override
    public String toString() {
        return "CustomerUpdate{" +
                "customerId=" + customerId +
                '}';
    }
}
