package com.satcom.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.satcom.platform.entity.BillingPlan;
import com.satcom.platform.mapper.BillingPlanMapper;
import com.satcom.platform.service.BillingPlanService;
import org.springframework.stereotype.Service;

@Service
public class BillingPlanServiceImpl extends ServiceImpl<BillingPlanMapper, BillingPlan> implements BillingPlanService {
}
