package com.laanto.it.paycenter.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.laanto.it.paycenter.domain.PayRequest;
import com.laanto.it.paycenter.repository.PayRequestRepository;

@Service
@Transactional(readOnly = true)
public class PayRequestService {
    private static final Logger logger = LoggerFactory.getLogger(PayRequestService.class);

    @Autowired
    private PayRequestRepository payRequestRepository;

    @Transactional
    public PayRequest save(PayRequest entity) {
        return payRequestRepository.save(entity);
    }


    public PayRequest findById(Long id) {
        return payRequestRepository.findOne(id);
    }

    public PayRequest findByOutTradeNo(String outTradeNo) {
        return payRequestRepository.findByOutTradeNo(outTradeNo);
    }
}
