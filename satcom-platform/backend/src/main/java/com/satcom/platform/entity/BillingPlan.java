package com.satcom.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资费套餐实体类
 * <p>系统资费和套餐信息</p>
 *
 * @author satcom
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@TableName("billing_plan")
public class BillingPlan {
    private Long id;
    private String planName;
    private String billingMode;
    private Double price;
    private String unit;
    private Integer cycleDays;
    private Boolean active;
    private LocalDateTime createdAt;
}
