package com.murong.nets.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * description
 *
 * @author yaochuang 2024/05/08 18:17
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 返回非null值的序列化
     */
    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json().serializationInclusion(JsonInclude.Include.NON_NULL).featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();
    }

}
