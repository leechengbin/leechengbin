package com.satcom.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 套餐视图对象
 * <p>用于返回给前端的套餐信息</p>
 *
 * @author satcom
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class BillingPlanVO {

    /**
     * 套餐ID
     */
    private Long id;

    /**
     * 套餐名称
     */
    private String planName;

    /**
     * 计费模式
     */
    private String billingMode;

    /**
     * 单价
     */
    private Double price;

    /**
     * 单位
     */
    private String unit;

    /**
     * 周期(天)
     */
    private Integer cycleDays;

    /**
     * 是否启用
     */
    private Boolean active;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
