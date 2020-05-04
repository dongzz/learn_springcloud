package com.dongz.springcloud;

import com.dongz.MySelfRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author dong
 * @date 2020/4/29 15:12
 * @desc
 */
@SpringBootApplication
//@EnableEurekaClient
@EnableDiscoveryClient
@RibbonClient(name = "CLOUD-PROVIDER-SERVICE",configuration = MySelfRule.class)
public class ConsumerOrderController {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerOrderController.class, args);
    }
}
