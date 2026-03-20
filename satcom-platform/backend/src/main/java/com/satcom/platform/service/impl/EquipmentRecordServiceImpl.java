package com.satcom.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.satcom.platform.entity.EquipmentRecord;
import com.satcom.platform.mapper.EquipmentRecordMapper;
import com.satcom.platform.service.EquipmentRecordService;
import org.springframework.stereotype.Service;

@Service
public class EquipmentRecordServiceImpl extends ServiceImpl<EquipmentRecordMapper, EquipmentRecord> implements EquipmentRecordService {
}
