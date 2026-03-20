package com.satcom.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.satcom.platform.common.ApiResponse;
import com.satcom.platform.dto.BillingPlanCreateDTO;
import com.satcom.platform.dto.BillingPlanUpdateDTO;
import com.satcom.platform.entity.BillingAccount;
import com.satcom.platform.entity.BillingPlan;
import com.satcom.platform.exception.BusinessException;
import com.satcom.platform.service.BillingAccountService;
import com.satcom.platform.service.BillingPlanService;
import com.satcom.platform.vo.BillingPlanVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账单管理控制器
 * <p>提供套餐和账户的增删改查功能</p>
 *
 * @author satcom
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingPlanService planService;
    private final BillingAccountService accountService;

    public BillingController(BillingPlanService planService, BillingAccountService accountService) {
        this.planService = planService;
        this.accountService = accountService;
    }

    /**
     * 获取套餐列表
     *
     * @return 套餐列表
     */
    @GetMapping("/plans")
    public ApiResponse<List<BillingPlanVO>> plans() {
        List<BillingPlan> list = planService.list(new LambdaQueryWrapper<BillingPlan>().orderByDesc(BillingPlan::getCreatedAt));
        List<BillingPlanVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return ApiResponse.ok(voList);
    }

    /**
     * 创建套餐
     *
     * @param dto 套餐信息
     * @return 创建的套餐
     */
    @PostMapping("/plans")
    public ApiResponse<BillingPlanVO> createPlan(@Valid @RequestBody BillingPlanCreateDTO dto) {
        BillingPlan plan = new BillingPlan();
        BeanUtils.copyProperties(dto, plan);
        plan.setCreatedAt(LocalDateTime.now());
        planService.save(plan);
        return ApiResponse.ok("套餐已创建", convertToVO(plan));
    }

    /**
     * 更新套餐
     *
     * @param id  套餐ID
     * @param dto 套餐信息
     * @return 更新后的套餐
     */
    @PutMapping("/plans/{id}")
    public ApiResponse<BillingPlanVO> updatePlan(@PathVariable Long id, @Valid @RequestBody BillingPlanUpdateDTO dto) {
        BillingPlan plan = planService.getById(id);
        if (plan == null) {
            throw new BusinessException("套餐不存在");
        }
        BeanUtils.copyProperties(dto, plan);
        planService.updateById(plan);
        return ApiResponse.ok("套餐已更新", convertToVO(plan));
    }

    /**
     * 删除套餐
     *
     * @param id 套餐ID
     * @return 操作结果
     */
    @DeleteMapping("/plans/{id}")
    public ApiResponse<String> deletePlan(@PathVariable Long id) {
        BillingPlan plan = planService.getById(id);
        if (plan == null) {
            throw new BusinessException("套餐不存在");
        }
        planService.removeById(id);
        return ApiResponse.ok("套餐已删除");
    }

    /**
     * 获取账户列表
     *
     * @param equipmentId 设备ID（可选）
     * @return 账户列表
     */
    @GetMapping("/accounts")
    public ApiResponse<List<BillingAccount>> accounts(@RequestParam(required = false) Long equipmentId) {
        LambdaQueryWrapper<BillingAccount> wrapper = new LambdaQueryWrapper<>();
        if (equipmentId != null) {
            wrapper.eq(BillingAccount::getEquipmentId, equipmentId);
        }
        wrapper.orderByDesc(BillingAccount::getUpdatedAt);
        return ApiResponse.ok(accountService.list(wrapper));
    }

    /**
     * 创建账户
     *
     * @param account 账户信息
     * @return 创建的账户
     */
    @PostMapping("/accounts")
    public ApiResponse<BillingAccount> createAccount(@RequestBody BillingAccount account) {
        account.setUpdatedAt(LocalDateTime.now());
        accountService.save(account);
        return ApiResponse.ok("账户已创建", account);
    }

    /**
     * 账户充值
     *
     * @param id     账户ID
     * @param request 充值请求
     * @return 充值后的账户
     */
    @PostMapping("/accounts/{id}/recharge")
    public ApiResponse<BillingAccount> recharge(@PathVariable Long id, @RequestBody RechargeRequest request) {
        BillingAccount account = accountService.getById(id);
        if (account == null) {
            return ApiResponse.fail("账户不存在");
        }
        account.setBalance(account.getBalance() + request.getAmount());
        account.setUpdatedAt(LocalDateTime.now());
        accountService.updateById(account);
        return ApiResponse.ok("充值成功", account);
    }

    /**
     * 更新账户到期时间
     *
     * @param id     账户ID
     * @param request 到期时间请求
     * @return 更新后的账户
     */
    @PostMapping("/accounts/{id}/expire")
    public ApiResponse<BillingAccount> updateExpire(@PathVariable Long id, @RequestBody ExpireRequest request) {
        BillingAccount account = accountService.getById(id);
        if (account == null) {
            return ApiResponse.fail("账户不存在");
        }
        account.setExpireAt(request.getExpireAt());
        account.setUpdatedAt(LocalDateTime.now());
        accountService.updateById(account);
        return ApiResponse.ok("已更新", account);
    }

    /**
     * 转换为VO对象
     *
     * @param plan 实体对象
     * @return 视图对象
     */
    private BillingPlanVO convertToVO(BillingPlan plan) {
        BillingPlanVO vo = new BillingPlanVO();
        BeanUtils.copyProperties(plan, vo);
        return vo;
    }

    public static class RechargeRequest {
        private Double amount;

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }
    }

    public static class ExpireRequest {
        private LocalDateTime expireAt;

        public LocalDateTime getExpireAt() {
            return expireAt;
        }

        public void setExpireAt(LocalDateTime expireAt) {
            this.expireAt = expireAt;
        }
    }
}
