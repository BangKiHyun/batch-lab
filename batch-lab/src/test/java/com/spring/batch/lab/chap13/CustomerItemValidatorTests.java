package com.spring.batch.lab.chap13;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CustomerItemValidatorTests {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private CustomerItemValidator validator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this); // 목 객체로 사용할 모든 객체를 초기화
        this.validator = new CustomerItemValidator(this.jdbcTemplate);
    }

    @Test
    void testValidCustomer() {

        //given
        CustomerUpdate customer = new CustomerUpdate(5L);

        //when
        ArgumentCaptor<Map<String, Long>> parametersMap = ArgumentCaptor.forClass(Map.class);
        when(this.jdbcTemplate.queryForObject(eq(CustomerItemValidator.FIND_CUSTOMER),
                parametersMap.capture(),
                eq(Long.class)))
                .thenReturn(2L);
        this.validator.validate(customer);

        //then
        assertEquals(5L, (long) parametersMap.getValue().get("id"));
    }
}
