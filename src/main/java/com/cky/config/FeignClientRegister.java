package com.cky.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;

public class FeignClientRegister implements BeanDefinitionRegistryPostProcessor {
    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    private static final String FEIGN_API_ANNOTATION = "com.cky.config.FeignApi";

    protected final Log logger = LogFactory.getLog(getClass());

    private final ConfigurableEnvironment environment;

    private ResourcePatternResolver resourcePatternResolver;

    private MetadataReaderFactory metadataReaderFactory;

    public FeignClientRegister(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        String basePackage = environment.getProperty("feign.client.scan.path");
        if (basePackage == null || basePackage.isEmpty()) {
            throw new RuntimeException("feign.client.scan.path配置未找到");
        }

        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(basePackage) + '/' + DEFAULT_RESOURCE_PATTERN;
        Resource[] resources = getResources(packageSearchPath);

        for (Resource resource : resources) {
            if (!resource.isReadable()) {
                logger.info("Ignored because not readable: " + resource);
            }

            MetadataReader metadataReader = getMetadataReader(resource);
            if (isCandidateComponent(metadataReader)) {
                Class<? extends ClassMetadata> clz = loadClass(metadataReader);
                BeanDefinitionBuilder baseBuilder = BeanDefinitionBuilder.genericBeanDefinition(FeignClientFactoryBean.class);
                AbstractBeanDefinition beanDefinition = baseBuilder.addConstructorArgValue(clz).addConstructorArgValue(environment).getBeanDefinition();
                beanDefinitionRegistry.registerBeanDefinition(clz.getName(), beanDefinition);
            }
        }
    }

    private Class<? extends ClassMetadata> loadClass(MetadataReader metadataReader) {
        String clzName = metadataReader.getClassMetadata().getClassName();
        try {
            return (Class<? extends ClassMetadata>) Class.forName(clzName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(this.environment.resolveRequiredPlaceholders(basePackage));
    }

    private Resource[] getResources(String packageSearchPath) {
        try {
            return getResourcePatternResolver().getResources(packageSearchPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MetadataReader getMetadataReader(Resource resource) {
        try {
            return getMetadataReaderFactory().getMetadataReader(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isCandidateComponent(MetadataReader metadataReader) {
        return metadataReader.getAnnotationMetadata().hasAnnotation(FEIGN_API_ANNOTATION);
    }

    private ResourcePatternResolver getResourcePatternResolver() {
        if (this.resourcePatternResolver == null) {
            this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        }
        return this.resourcePatternResolver;
    }

    public final MetadataReaderFactory getMetadataReaderFactory() {
        if (this.metadataReaderFactory == null) {
            this.metadataReaderFactory = new CachingMetadataReaderFactory();
        }
        return this.metadataReaderFactory;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
