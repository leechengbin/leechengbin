package com.satcom.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("billing_account")
public class BillingAccount {
    private Long id;
    private Long equipmentId;
    private Long planId;
    private Double balance;
    private LocalDateTime expireAt;
    private LocalDateTime updatedAt;
}
