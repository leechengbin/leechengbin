package com.satcom.platform.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 套餐创建 DTO
 * <p>用于创建新的资费套餐</p>
 *
 * @author satcom
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class BillingPlanCreateDTO {

    /**
     * 套餐名称
     */
    @NotBlank(message = "套餐名称不能为空")
    @Size(max = 100, message = "套餐名称长度不能超过100")
    private String planName;

    /**
     * 计费模式：MONTHLY-包月, TIME-按时长, TRAFFIC-按流量
     */
    @NotBlank(message = "计费模式不能为空")
    private String billingMode;

    /**
     * 单价
     */
    @NotNull(message = "单价不能为空")
    @Positive(message = "单价必须为正数")
    private Double price;

    /**
     * 单位
     */
    @NotBlank(message = "单位不能为空")
    @Size(max = 50, message = "单位长度不能超过50")
    private String unit;

    /**
     * 周期(天)
     */
    @NotNull(message = "周期不能为空")
    @Positive(message = "周期必须为正数")
    private Integer cycleDays;

    /**
     * 是否启用
     */
    private Boolean active = true;
}
