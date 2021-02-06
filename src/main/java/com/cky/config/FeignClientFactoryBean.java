package com.cky.config;

import feign.Feign;
import feign.Request;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.env.ConfigurableEnvironment;

import static feign.Feign.builder;

public class FeignClientFactoryBean<T> implements FactoryBean<T> {
    private final Class<T> objectType;
    private final ConfigurableEnvironment environment;

    public FeignClientFactoryBean(Class<T> objectType, ConfigurableEnvironment environment) {
        this.objectType = objectType;
        this.environment = environment;
    }

    @Override
    public T getObject() {
        String originalUrl = objectType.getAnnotation(FeignApi.class).serviceUrl();
        String url = environment.resolvePlaceholders(originalUrl);

        return getFeignBuilder().target(objectType, url);
    }

    private Feign.Builder getFeignBuilder() {
        return builder().encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 3500))
                .retryer(new Retryer.Default(5000, 5000, 3));
    }

    @Override
    public Class<?> getObjectType() {
        return objectType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
