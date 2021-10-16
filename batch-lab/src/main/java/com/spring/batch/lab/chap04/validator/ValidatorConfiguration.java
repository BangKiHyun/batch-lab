package com.spring.batch.lab.chap04.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class ValidatorConfiguration {

    private final ParameterValidator parameterValidator;

    @Bean
    public DefaultJobParametersValidator defaultValidator() {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();

        // 필수 파라미터
        validator.setRequiredKeys(new String[]{"fileName"});

        // 필수가 아닌 파라미터
        validator.setOptionalKeys(new String[]{"name"});

        // 위에서 정의한 두 가지 파라미터 외에 변수가 전달되면 검증 실패
        // 만약에 옵션 키가 구성돼 있지 않고 필수 키만 구성돼 있다면, 필수 키를 전달하기만 하면 그 외 어떤 키를 조합하더라도 유효성 검증 통과
        return validator;
    }


    // 원하는 Validator 합성
    @Bean
    public CompositeJobParametersValidator compositeValidator() {
        final CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        defaultValidator().afterPropertiesSet();

        validator.setValidators(
                Arrays.asList(parameterValidator,
                        defaultValidator()));
        return validator;
    }
}
