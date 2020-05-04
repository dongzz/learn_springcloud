package com.dongz.springcloud.controllers;

import com.dongz.springcloud.entities.Payment;
import com.dongz.springcloud.services.PaymentService;
import com.dongz.springcloud.utils.Result;
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
        log.info("method 1； 线程池，线程ID：" + Thread.currentThread().getId() + ",线程name：" + Thread.currentThread().getName());
        return new Result<>(200, "查询成功", payment);
    }

    @GetMapping("/getById2/{id}")
    @HystrixCommand(fallbackMethod = "getById",commandProperties = {
            //设置这个线程的超时时间是3s，3s内是正常的业务逻辑，超过3s调用fallbackMethod指定的方法进行处理
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "3000")
    })
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
        log.info("method 2；线程池，线程ID：" + Thread.currentThread().getId() + ",线程name：" + Thread.currentThread().getName());
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
