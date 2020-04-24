package com.dongz.springcloud.services.impl;

import com.dongz.springcloud.dao.PaymentDao;
import com.dongz.springcloud.entities.Payment;
import com.dongz.springcloud.services.PaymentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author dong
 * @date 2020/4/24 15:42
 * @desc
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentDao paymentDao;

    @Override
    public int create(Payment payment) {
        return paymentDao.create(payment);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentDao.getPaymentById(id);
    }
}
