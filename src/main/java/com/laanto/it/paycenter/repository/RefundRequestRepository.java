package com.laanto.it.paycenter.repository;

import com.laanto.it.paycenter.domain.PayRequest;
import com.laanto.it.paycenter.domain.RefundRequest;

public interface RefundRequestRepository extends BaseRepository<RefundRequest, Long> {

    RefundRequest findByOutRefundNo(String outRefundNo);
}

