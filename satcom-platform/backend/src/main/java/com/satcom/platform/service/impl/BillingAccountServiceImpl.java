package com.satcom.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.satcom.platform.entity.BillingAccount;
import com.satcom.platform.mapper.BillingAccountMapper;
import com.satcom.platform.service.BillingAccountService;
import org.springframework.stereotype.Service;

@Service
public class BillingAccountServiceImpl extends ServiceImpl<BillingAccountMapper, BillingAccount> implements BillingAccountService {
}
