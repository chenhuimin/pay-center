package com.laanto.it.paycenter.service;

import com.laanto.it.paycenter.domain.PayRequest;
import com.laanto.it.paycenter.domain.RefundRequest;
import com.laanto.it.paycenter.repository.PayRequestRepository;
import com.laanto.it.paycenter.repository.RefundRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RefundRequestService {
    private static final Logger logger = LoggerFactory.getLogger(RefundRequestService.class);

    @Autowired
    private RefundRequestRepository refundRequestRepository;

    @Transactional
    public RefundRequest save(RefundRequest entity) {
        return refundRequestRepository.save(entity);
    }


    public RefundRequest findByOutRefundNo(String outRefundNo) {
        return refundRequestRepository.findByOutRefundNo(outRefundNo);
    }
}
