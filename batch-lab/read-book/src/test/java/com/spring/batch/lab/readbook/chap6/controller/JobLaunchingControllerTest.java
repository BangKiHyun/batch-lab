package com.spring.batch.lab.readbook.chap6.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.batch.lab.readbook.chap6.controller.dto.request.JobLauncherRequest;
import com.spring.batch.lab.readbook.chap6.job.RestJobConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobLaunchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @DisplayName("Rest API를 통한 job 실행 테스트")
    @Test
    public void runJobTest() throws Exception {
        //given
        JobLauncherRequest request = new JobLauncherRequest(RestJobConfiguration.JOB_NAME, new Properties());
        final String requestStr = objectMapper.writeValueAsString(request);

        //when
        MvcResult mvcResult = mockMvc.perform(
                post("/job/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        ExitStatus actual = objectMapper.readValue(responseString, new TypeReference<>() {
        });

        //then
        assertThat(actual).isEqualTo(ExitStatus.COMPLETED);
    }
}