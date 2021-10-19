package com.spring.batch.lab.readbook.chap4.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ParameterValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        String fileName = parameters.getString("fileName");

        // fileName 파라미터가 있는지 check
        if (!StringUtils.hasText(fileName)) {
            throw new JobParametersInvalidException("fileName parameter is missing");
            // 파일이 .csv로 끝나는지 check
        } else if (!StringUtils.endsWithIgnoreCase(fileName, "csv")) {
            throw new JobParametersInvalidException("fileName parameter does not use the csv file extension");
        }
    }
}
