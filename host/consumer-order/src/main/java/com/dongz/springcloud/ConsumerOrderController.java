package com.dongz.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author dong
 * @date 2020/4/29 15:12
 * @desc
 */
@SpringBootApplication
@EnableEurekaClient
public class ConsumerOrderController {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerOrderController.class, args);
    }
}
