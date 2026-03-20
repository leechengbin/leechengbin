package com.satcom.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.satcom.platform.entity.MonitoringStatus;
import com.satcom.platform.mapper.MonitoringStatusMapper;
import com.satcom.platform.service.MonitoringStatusService;
import org.springframework.stereotype.Service;

@Service
public class MonitoringStatusServiceImpl extends ServiceImpl<MonitoringStatusMapper, MonitoringStatus> implements MonitoringStatusService {
}
