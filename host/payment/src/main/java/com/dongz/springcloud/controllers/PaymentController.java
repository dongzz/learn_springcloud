package com.dongz.springcloud.controllers;

import com.dongz.springcloud.entities.Payment;
import com.dongz.springcloud.services.PaymentService;
import com.dongz.springcloud.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dong
 * @date 2020/4/24 15:44
 * @desc
 */
@RestController
@RequestMapping("/api/pay")
@Slf4j
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


}
