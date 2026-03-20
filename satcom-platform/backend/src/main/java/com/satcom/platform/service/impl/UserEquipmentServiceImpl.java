package com.satcom.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.satcom.platform.entity.Equipment;
import com.satcom.platform.entity.User;
import com.satcom.platform.entity.UserEquipment;
import com.satcom.platform.mapper.UserEquipmentMapper;
import com.satcom.platform.service.EquipmentService;
import com.satcom.platform.service.UserEquipmentService;
import com.satcom.platform.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserEquipmentServiceImpl extends ServiceImpl<UserEquipmentMapper, UserEquipment> implements UserEquipmentService {

    private final UserService userService;
    private final EquipmentService equipmentService;

    public UserEquipmentServiceImpl(UserService userService, EquipmentService equipmentService) {
        this.userService = userService;
        this.equipmentService = equipmentService;
    }

    @Override
    public List<Long> getAuthorizedEquipmentIds(Long userId) {
        User user = userService.getById(userId);
        if (user != null && "ADMIN".equals(user.getRoleCode())) {
            return equipmentService.list().stream()
                    .map(Equipment::getId)
                    .collect(Collectors.toList());
        }

        LambdaQueryWrapper<UserEquipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEquipment::getUserId, userId);
        wrapper.eq(UserEquipment::getViewPermission, true);
        return this.list(wrapper).stream()
                .map(UserEquipment::getEquipmentId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasViewPermission(Long userId, Long equipmentId) {
        LambdaQueryWrapper<UserEquipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEquipment::getUserId, userId);
        wrapper.eq(UserEquipment::getEquipmentId, equipmentId);
        wrapper.eq(UserEquipment::getViewPermission, true);
        return this.exists(wrapper);
    }

    @Override
    public boolean hasEditPermission(Long userId, Long equipmentId) {
        LambdaQueryWrapper<UserEquipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEquipment::getUserId, userId);
        wrapper.eq(UserEquipment::getEquipmentId, equipmentId);
        wrapper.eq(UserEquipment::getEditPermission, true);
        return this.exists(wrapper);
    }

    @Override
    public void grantPermissions(Long userId, List<Long> equipmentIds, boolean viewPermission, boolean editPermission) {
        // 先删除现有的权限
        LambdaQueryWrapper<UserEquipment> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserEquipment::getUserId, userId);
        this.remove(deleteWrapper);

        // 添加新的权限
        LocalDateTime now = LocalDateTime.now();
        List<UserEquipment> permissions = equipmentIds.stream().map(equipmentId -> {
            UserEquipment userEquipment = new UserEquipment();
            userEquipment.setUserId(userId);
            userEquipment.setEquipmentId(equipmentId);
            userEquipment.setViewPermission(viewPermission);
            userEquipment.setEditPermission(editPermission);
            userEquipment.setCreatedAt(now);
            userEquipment.setUpdatedAt(now);
            return userEquipment;
        }).collect(Collectors.toList());

        this.saveBatch(permissions);
    }

    @Override
    public void revokePermissions(Long userId, List<Long> equipmentIds) {
        LambdaQueryWrapper<UserEquipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEquipment::getUserId, userId);
        wrapper.in(UserEquipment::getEquipmentId, equipmentIds);
        this.remove(wrapper);
    }

    @Override
    public List<UserEquipment> getPermissionsByUserId(Long userId) {
        LambdaQueryWrapper<UserEquipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEquipment::getUserId, userId);
        return this.list(wrapper);
    }

    @Override
    public void savePermissions(Long userId, List<UserEquipmentService.EquipmentPermissionItem> permissions) {
        // 先删除现有的所有权限
        LambdaQueryWrapper<UserEquipment> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserEquipment::getUserId, userId);
        this.remove(deleteWrapper);

        // 添加新的权限
        LocalDateTime now = LocalDateTime.now();
        List<UserEquipment> userEquipments = permissions.stream()
                .filter(item -> item.isViewPermission() || item.isEditPermission()) // 只保存有权限的项
                .map(item -> {
                    UserEquipment userEquipment = new UserEquipment();
                    userEquipment.setUserId(userId);
                    userEquipment.setEquipmentId(item.getEquipmentId());
                    userEquipment.setViewPermission(item.isViewPermission());
                    userEquipment.setEditPermission(item.isEditPermission());
                    userEquipment.setCreatedAt(now);
                    userEquipment.setUpdatedAt(now);
                    return userEquipment;
                })
                .collect(Collectors.toList());

        if (!userEquipments.isEmpty()) {
            this.saveBatch(userEquipments);
        }
    }
}
