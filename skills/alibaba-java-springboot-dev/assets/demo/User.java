package com.company.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * <p>使用 MyBatis-Plus 注解配置表映射关系</p>
 *
 * @author zhangsan
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@TableName("t_user")
public class User {

    /**
     * 主键 ID
     * <p>使用雪花算法自动生成唯一 ID</p>
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    @TableField(select = false)
    private String password;

    /**
     * 创建时间
     * <p>插入时自动填充当前时间</p>
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * <p>插入和更新时自动填充当前时间</p>
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     * <p>0-未删除，1-已删除</p>
     */
    @TableLogic
    private Integer deleted;
}
