package com.laanto.it.paycenter.repository;

import com.laanto.it.paycenter.domain.PayRequest;

public interface PayRequestRepository extends BaseRepository<PayRequest, Long> {

    PayRequest findByOutTradeNo(String outTradeNo);
}
