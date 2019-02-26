package com.cky;

import com.cky.DO.MobileInfo;
import com.cky.DO.Result;
import com.cky.api.MobileApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static com.google.common.collect.ImmutableMap.of;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class.getName());

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");

        MobileApi mobileApi = context.getBean(MobileApi.class);

        LOGGER.info("--- 使用Param提交示例 ---");
        Result<MobileInfo> result = mobileApi.getByParam("13038498443", "", "d62771a0532fdb49a207971786ea2b00");
        LOGGER.info(result.toString());

        LOGGER.info("--- 使用map提交示例 ---");
        result = mobileApi.getByQueryMap(of("phone", "13038498443", "dtype", "", "key", "d62771a0532fdb49a207971786ea2b00"));
        LOGGER.info(result.toString());


    }
}
