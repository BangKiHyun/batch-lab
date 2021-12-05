package com.spring.batch.lab.readbook.chap7.mapper;

import com.spring.batch.lab.readbook.chap7.model.CustomerJPA;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerJpaRowMapper implements RowMapper<CustomerJPA> {

    @Override
    public CustomerJPA mapRow(ResultSet rs, int rowNum) throws SQLException {
        return CustomerJPA.builder()
                .id(rs.getLong("id"))
                .address(rs.getString("address"))
                .city(rs.getString("city"))
                .firstName(rs.getString("firstName"))
                .lastName(rs.getString("lastName"))
                .middleInitial(rs.getString("middleInitial"))
                .state(rs.getString("state"))
                .zipCode(rs.getString("zipCode"))
                .build();
    }
}
