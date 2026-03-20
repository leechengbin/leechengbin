package com.satcom.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.satcom.platform.entity.UserEquipment;

import java.util.List;

public interface UserEquipmentService {
    List<Long> getAuthorizedEquipmentIds(Long userId);
    boolean hasViewPermission(Long userId, Long equipmentId);
    boolean hasEditPermission(Long userId, Long equipmentId);
    void grantPermissions(Long userId, List<Long> equipmentIds, boolean viewPermission, boolean editPermission);
    void revokePermissions(Long userId, List<Long> equipmentIds);
    List<UserEquipment> getPermissionsByUserId(Long userId);
    void savePermissions(Long userId, List<EquipmentPermissionItem> permissions);
    
    // 权限项DTO
    class EquipmentPermissionItem {
        private Long equipmentId;
        private boolean viewPermission;
        private boolean editPermission;
        
        public Long getEquipmentId() {
            return equipmentId;
        }
        public void setEquipmentId(Long equipmentId) {
            this.equipmentId = equipmentId;
        }
        public boolean isViewPermission() {
            return viewPermission;
        }
        public void setViewPermission(boolean viewPermission) {
            this.viewPermission = viewPermission;
        }
        public boolean isEditPermission() {
            return editPermission;
        }
        public void setEditPermission(boolean editPermission) {
            this.editPermission = editPermission;
        }
    }
}
