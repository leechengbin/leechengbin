package com.satcom.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.satcom.platform.common.ApiResponse;
import com.satcom.platform.entity.DispatchTask;
import com.satcom.platform.service.DispatchTaskService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    private final DispatchTaskService dispatchService;

    public DispatchController(DispatchTaskService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @GetMapping("/tasks")
    public ApiResponse<List<DispatchTask>> list(@RequestParam(required = false) String status) {
        LambdaQueryWrapper<DispatchTask> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq(DispatchTask::getStatus, status);
        }
        wrapper.orderByDesc(DispatchTask::getCreatedAt);
        return ApiResponse.ok(dispatchService.list(wrapper));
    }

    @PostMapping("/tasks")
    public ApiResponse<DispatchTask> create(@RequestBody DispatchTask task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        if (task.getStatus() == null) {
            task.setStatus("NEW");
        }
        dispatchService.save(task);
        return ApiResponse.ok("任务已创建", task);
    }

    @PutMapping("/tasks/{id}")
    public ApiResponse<DispatchTask> update(@PathVariable Long id, @RequestBody DispatchTask task) {
        task.setId(id);
        task.setUpdatedAt(LocalDateTime.now());
        dispatchService.updateById(task);
        return ApiResponse.ok("任务已更新", task);
    }

    @DeleteMapping("/tasks/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        dispatchService.removeById(id);
        return ApiResponse.ok("任务已删除");
    }
}
