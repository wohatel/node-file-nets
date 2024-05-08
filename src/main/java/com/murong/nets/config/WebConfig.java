package com.murong.nets.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * description
 *
 * @author yaochuang 2024/05/08 18:17
 */
@Configuration
public class WebConfig {

    /**
     * 返回非null值的序列化
     */
    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json().serializationInclusion(JsonInclude.Include.NON_NULL).build();
    }

}
