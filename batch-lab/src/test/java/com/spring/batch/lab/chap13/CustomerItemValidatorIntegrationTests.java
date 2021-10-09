package com.spring.batch.lab.chap13;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@JdbcTest
public class CustomerItemValidatorIntegrationTests {

    @Autowired
    private DataSource dataSource;

    private CustomerItemValidator validator;

    @BeforeEach
    public void setUp() {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        this.validator = new CustomerItemValidator(template);
    }

    @Test
    public void testNoCustomers() {
        CustomerUpdate customer = new CustomerUpdate(-5L);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> this.validator.validate(customer));

        assertEquals("Customer id -5 was not able to be found", exception.getMessage());
    }

    @Test
    public void testCustomers() {
        final CustomerUpdate customer = new CustomerUpdate(5L);
        this.validator.validate(customer);
    }
}
