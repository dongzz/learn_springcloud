package com.dongz.springcloud.services;

import com.dongz.springcloud.entities.Payment;
import com.dongz.springcloud.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author dong
 * @date 2020/5/4 10:03
 * @desc
 */
@Component
@FeignClient(value = "payment-service")  //指定调用哪个微服务
public interface PaymentService {
    @GetMapping("/api/pay/getById/{id}")
    Result<Payment> getById(@PathVariable("id") Long id) ;

    @GetMapping("/api/pay/getById2/{id}")
    Result<Payment> getById2(@PathVariable("id") Long id) ;
}
