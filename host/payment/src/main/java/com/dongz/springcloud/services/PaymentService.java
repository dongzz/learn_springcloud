package com.dongz.springcloud.services;

import com.dongz.springcloud.entities.Payment;
import org.apache.ibatis.annotations.Param;

/**
 * @author dong
 * @date 2020/4/24 15:41
 * @desc
 */
public interface PaymentService {

    int create(Payment payment);

    Payment getPaymentById(@Param("id") Long id);
}
