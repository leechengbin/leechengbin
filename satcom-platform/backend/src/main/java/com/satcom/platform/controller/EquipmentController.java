package com.satcom.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.satcom.platform.common.ApiResponse;
import com.satcom.platform.common.PageResponse;
import com.satcom.platform.entity.Equipment;
import com.satcom.platform.entity.EquipmentRecord;
import com.satcom.platform.service.EquipmentRecordService;
import com.satcom.platform.service.EquipmentService;
import com.satcom.platform.service.UserEquipmentService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final EquipmentRecordService recordService;
    private final UserEquipmentService userEquipmentService;

    public EquipmentController(EquipmentService equipmentService, EquipmentRecordService recordService, UserEquipmentService userEquipmentService) {
        this.equipmentService = equipmentService;
        this.recordService = recordService;
        this.userEquipmentService = userEquipmentService;
    }

    // 获取当前用户ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        // 这里简化处理，实际应该从用户信息中获取ID
        // 由于我们没有实现完整的用户认证，暂时返回1（管理员）
        return 1L;
    }

    @GetMapping
    public ApiResponse<PageResponse<Equipment>> list(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(defaultValue = "false") boolean all) {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();

        // 非管理员需要根据权限过滤
        if (!all && userId != null) {
            List<Long> authorizedEquipmentIds = userEquipmentService.getAuthorizedEquipmentIds(userId);
            if (!authorizedEquipmentIds.isEmpty()) {
                wrapper.in(Equipment::getId, authorizedEquipmentIds);
            } else {
                // 没有权限，返回空列表
                return ApiResponse.ok(new PageResponse<>(0L, List.of()));
            }
        }

        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Equipment::getAssetCode, keyword).or().like(Equipment::getModel, keyword);
        }

        Page<Equipment> result = equipmentService.page(new Page<>(page, size), wrapper);
        return ApiResponse.ok(new PageResponse<>(result.getTotal(), result.getRecords()));
    }

    @PostMapping
    public ApiResponse<Equipment> create(@Valid @RequestBody Equipment request) {
        // 这里可以添加创建权限检查
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        equipmentService.save(request);
        return ApiResponse.ok("创建成功", request);
    }

    @PutMapping("/{id}")
    public ApiResponse<Equipment> update(@PathVariable Long id, @Valid @RequestBody Equipment request) {
        Long userId = getCurrentUserId();
        if (userId != null && !userEquipmentService.hasEditPermission(userId, id)) {
            return ApiResponse.fail("无编辑权限");
        }
        
        request.setId(id);
        request.setUpdatedAt(LocalDateTime.now());
        equipmentService.updateById(request);
        return ApiResponse.ok("更新成功", request);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        // 这里可以添加删除权限检查
        equipmentService.removeById(id);
        return ApiResponse.ok("删除成功", null);
    }

    @PostMapping("/{id}/records")
    public ApiResponse<EquipmentRecord> addRecord(@PathVariable Long id, @Valid @RequestBody EquipmentRecord record) {
        Long userId = getCurrentUserId();
        if (userId != null && !userEquipmentService.hasEditPermission(userId, id)) {
            return ApiResponse.fail("无编辑权限");
        }
        
        record.setEquipmentId(id);
        record.setCreatedAt(LocalDateTime.now());
        recordService.save(record);
        return ApiResponse.ok("记录已添加", record);
    }

    @GetMapping("/{id}/records")
    public ApiResponse<List<EquipmentRecord>> records(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId != null && !userEquipmentService.hasViewPermission(userId, id)) {
            return ApiResponse.fail("无查看权限");
        }
        
        List<EquipmentRecord> records = recordService.list(new LambdaQueryWrapper<EquipmentRecord>()
                .eq(EquipmentRecord::getEquipmentId, id)
                .orderByDesc(EquipmentRecord::getRecordAt));
        return ApiResponse.ok(records);
    }

    @PostMapping("/{id}/lock")
    public ApiResponse<Void> lock(@PathVariable Long id, @RequestBody LockRequest request) {
        Long userId = getCurrentUserId();
        if (userId != null && !userEquipmentService.hasEditPermission(userId, id)) {
            return ApiResponse.fail("无编辑权限");
        }
        
        Equipment equipment = equipmentService.getById(id);
        if (equipment == null) {
            return ApiResponse.fail("设备不存在");
        }
        equipment.setLocked(request.isLocked());
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentService.updateById(equipment);
        return ApiResponse.ok("状态已更新", null);
    }

    @Data
    public static class LockRequest {
        private boolean locked;
    }
}

