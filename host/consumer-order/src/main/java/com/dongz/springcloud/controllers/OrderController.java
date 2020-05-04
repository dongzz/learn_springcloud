package com.dongz.springcloud.controllers;

import com.dongz.springcloud.entities.Payment;
import com.dongz.springcloud.services.PaymentService;
import com.dongz.springcloud.utils.Result;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author dong
 * @date 2020/4/29 15:15
 * @desc
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    public static final String PAYMENT_URL = "http://payment-service";
    @Resource
    PaymentService service;

    @Resource
    private RestTemplate restTemplate;

    @PostMapping("create")
    public Result<Payment> create(Payment payment) {
        return restTemplate.postForObject(PAYMENT_URL + "/api/pay/create", payment, Result.class);
    }

    @GetMapping("/getById/{id}")
    public Result<Payment> getById(@PathVariable("id") Long id) {
        return restTemplate.getForObject(PAYMENT_URL + "/api/pay/getById/" + id, Result.class);
    }

    @GetMapping("/getById2/{id}")
    public Result<Payment> getById2(@PathVariable("id") Long id) {
        return service.getById2(id);
    }

    @HystrixCommand(fallbackMethod = "getById",commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "3000")
    })
    @GetMapping("/getById3/{id}")
    public Result<Payment> getById3(@PathVariable("id") Long id) throws InterruptedException {
        TimeUnit.SECONDS.sleep(4);
        return service.getById(id);
    }
}
