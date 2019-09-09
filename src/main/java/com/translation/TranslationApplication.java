package com.translation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableTransactionManagement
@EnableSwagger2
@EnableScheduling
@SpringBootApplication
@MapperScan(basePackages = "com.translation.dao")
public class TranslationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TranslationApplication.class, args);
    }

}
