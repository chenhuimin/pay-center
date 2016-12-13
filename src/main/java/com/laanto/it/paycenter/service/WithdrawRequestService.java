package com.laanto.it.paycenter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.laanto.it.paycenter.domain.WithdrawRequest;
import com.laanto.it.paycenter.repository.WithdrawRequestRepository;

@Service
@Transactional(readOnly = true)
public class WithdrawRequestService {
	
	@Autowired
	private WithdrawRequestRepository withdrawRequestRepository;
	
	@Transactional
	public WithdrawRequest save(WithdrawRequest entity){
		return withdrawRequestRepository.save(entity);
	}
}
