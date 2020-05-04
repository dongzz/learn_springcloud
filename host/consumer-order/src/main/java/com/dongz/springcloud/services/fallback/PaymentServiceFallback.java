package com.dongz.springcloud.services.fallback;

import com.dongz.springcloud.controllers.OrderController;
import com.dongz.springcloud.entities.Payment;
import com.dongz.springcloud.services.PaymentService;
import com.dongz.springcloud.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author dong
 * @date 2020/5/4 17:01
 * @desc
 */
@Component
@Slf4j
public class PaymentServiceFallback implements PaymentService {

    @Override
    public Result<Payment> getById(Long id) {
        log.error("访问异常，getById");
        return null;
    }

    @Override
    public Result<Payment> getById2(Long id) {
        log.error("访问异常，getById2");
        return null;
    }
}
