package com.dongz.springcloud.controllers;

import com.dongz.springcloud.entities.Payment;
import com.dongz.springcloud.utils.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

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
    private RestTemplate restTemplate;

    @PostMapping("create")
    public Result<Payment> create(Payment payment) {
        return restTemplate.postForObject(PAYMENT_URL + "/api/pay/create", payment, Result.class);
    }

    @GetMapping("/getById/{id}")
    public Result<Payment> getById(@PathVariable("id") Long id) {
        return restTemplate.getForObject(PAYMENT_URL + "/api/pay/getById/" + id, Result.class);
    }
}
