package com.satcom.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.satcom.platform.entity.Equipment;
import com.satcom.platform.mapper.EquipmentMapper;
import com.satcom.platform.service.EquipmentService;
import org.springframework.stereotype.Service;

@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {
}
