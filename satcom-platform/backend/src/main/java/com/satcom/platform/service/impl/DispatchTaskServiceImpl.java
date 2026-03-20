package com.satcom.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.satcom.platform.entity.DispatchTask;
import com.satcom.platform.mapper.DispatchTaskMapper;
import com.satcom.platform.service.DispatchTaskService;
import org.springframework.stereotype.Service;

@Service
public class DispatchTaskServiceImpl extends ServiceImpl<DispatchTaskMapper, DispatchTask> implements DispatchTaskService {
}
