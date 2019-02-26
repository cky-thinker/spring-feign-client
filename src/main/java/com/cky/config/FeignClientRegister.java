package com.cky.config;

import com.cky.util.PropertiesUtil;
import feign.Request;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static feign.Feign.Builder;
import static feign.Feign.builder;

@Component
public class FeignClientRegister implements BeanFactoryPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class.getName());

    private final String HTTP_HEAD = "http://";

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String scanPath = PropertiesUtil.getProperties().getProperty("feign.client.scan.path");

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
}
