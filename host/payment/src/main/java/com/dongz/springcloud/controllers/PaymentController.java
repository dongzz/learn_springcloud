package com.dongz.springcloud.controllers;

import cn.hutool.core.util.IdUtil;
import com.dongz.springcloud.entities.Payment;
import com.dongz.springcloud.services.PaymentService;
import com.dongz.springcloud.utils.Result;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author dong
 * @date 2020/4/24 15:44
 * @desc
 */
@RestController
@RequestMapping("/api/pay")
@Slf4j
@DefaultProperties(defaultFallback = "getById")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Resource
    private DiscoveryClient discoveryClient;

    @PostMapping("/create")
    public Result<Payment> create(@RequestBody Payment payment) {
        int result = paymentService.create(payment);
        log.info("***** 插入结果： " + result);

        if (result == 0) {
            return new Result<>(444, "插入失败");
        }
        return new Result<>(200, "插入成功");
    }

    @GetMapping("/getById/{id}")
    public Result<Payment> getById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);

        if (payment == null) {
            return new Result<>(444, "查询失败");
        }
        log.info("method 1； 线程池，线程池ID：" + Thread.currentThread().getId() + ",线程池name：" + Thread.currentThread().getName());
        return new Result<>(200, "查询成功", payment);
    }

    @GetMapping("/getById2/{id}")
    @HystrixCommand
    public Result<Payment> getById2(@PathVariable Long id) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Payment payment = paymentService.getPaymentById(id);

        if (payment == null) {
            return new Result<>(444, "查询失败");
        }
        log.info("method 2；线程池，线程池ID：" + Thread.currentThread().getId() + ",线程池name：" + Thread.currentThread().getName());
        return new Result<>(200, "查询成功", payment);
    }

    @GetMapping("/getDiscovery")
    public List<String> getDiscovery() {
        return discoveryClient.getServices();
    }

    @GetMapping("/getServiceInfo/{name}")
    public List<ServiceInstance> getServiceInfo(@PathVariable String name) {
        return discoveryClient.getInstances(name);
    }

    // ==== 服务熔断
    @GetMapping("/paymentCircuitBreaker/{id}")
    @HystrixCommand(fallbackMethod = "paymentCircuitBreakerFallback", commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),//是否开启断路器
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),//请求次数
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),//时间窗口期（时间范围）
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60"),//失败率达到多少后跳闸
    })
    public String paymentCircuitBreaker(@PathVariable("id") Long id) {

        if (id < 0) {
            throw new RuntimeException("id 不能为负");
        }
        return "线程池：" + Thread.currentThread().getName() + "流水号" + IdUtil.simpleUUID();
    }

    public String paymentCircuitBreakerFallback(@PathVariable("id") Long id) {
        return "id不能为负，线程池：" + Thread.currentThread().getName() + "流水号" + IdUtil.simpleUUID();
    }
}
