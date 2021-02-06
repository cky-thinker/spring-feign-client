package com.cky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
@PropertySource("classpath:application.properties")
public class FeignApiConfig {
    @Bean
    public FeignClientRegister getFeignClientRegister(ConfigurableEnvironment environment) {
        return new FeignClientRegister(environment);
    }
}
