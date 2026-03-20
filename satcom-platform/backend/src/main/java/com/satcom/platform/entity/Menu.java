package com.satcom.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String path;
    private String icon;
    private Long parentId;
    private Integer orderNum;
    private Boolean visible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}