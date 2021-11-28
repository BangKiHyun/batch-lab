package com.spring.batch.lab.readbook.chap7.mapper;

import com.spring.batch.lab.readbook.chap7.model.CustomerDB;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDBRowMapper implements RowMapper<CustomerDB> {

    @Override
    public CustomerDB mapRow(ResultSet rs, int rowNum) throws SQLException {
        return CustomerDB.builder()
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
