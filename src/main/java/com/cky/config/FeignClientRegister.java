package com.cky.config;

import feign.Request;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static feign.Feign.Builder;
import static feign.Feign.builder;

@Component
public class FeignClientRegister implements BeanFactoryPostProcessor {
    private final String HTTP_HEAD = "http://";

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String scanPath = getProperties().getProperty("feign.client.scan.path");

        if (scanPath == null || scanPath.isEmpty()) {
            throw new RuntimeException("feign.client.scan.path配置未找到,请在resources/application.properties中添加该配置.");
        }

        scanFeignApi(scanPath).ifPresent((feignApiStrs) -> {
            Builder feignBuilder = getFeignBuilder();
            feignApiStrs.forEach((feignApiStr) -> {
                try {
                    Class<?> feignApi = Class.forName(feignApiStr);
                    String url = feignApi.getAnnotation(FeignApi.class).serviceUrl();
                    if (url.indexOf(HTTP_HEAD) != 0) {
                        url = HTTP_HEAD + url;
                    }

                    Object feignApiBean = feignBuilder.target(feignApi, url);

                    beanFactory.registerSingleton(feignApi.getName(), feignApiBean);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }


    private Builder getFeignBuilder() {
        return builder().encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 3500))
                .retryer(new Retryer.Default(5000, 5000, 3));
    }

    private Optional<List<String>> scanFeignApi(String scanPath) {
        ScanResult scanResult = new FastClasspathScanner(scanPath).matchClassesWithAnnotation(FeignApi.class, (clz) -> {
        }).scan();

        if (scanResult != null) {
            return Optional.of(scanResult.getNamesOfAllInterfaceClasses());
        }
        return Optional.empty();
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        try {
            File file = ResourceUtils.getFile("classpath:application.properties");
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
