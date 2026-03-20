package com.satcom.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dispatch_task")
public class DispatchTask {
    private Long id;
    private String title;
    private String level;
    private String status;
    private String targetUnit;
    private String message;
    private String contact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
