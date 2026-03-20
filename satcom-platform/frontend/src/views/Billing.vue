<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">资费管理</div>
        <div class="page-sub">套餐与余额管理</div>
      </div>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新增套餐
      </el-button>
    </div>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>资费套餐</span>
          <el-input
            v-model="searchText"
            placeholder="搜索套餐名称"
            style="width: 240px"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </template>
      <el-table :data="filteredPlans" style="width: 100%" v-loading="loading">
        <el-table-column prop="planName" label="套餐名称" min-width="150" />
        <el-table-column prop="billingMode" label="计费模式" width="120">
          <template #default="scope">
            <el-tag :type="getBillingModeType(scope.row.billingMode)">
              {{ getBillingModeText(scope.row.billingMode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="120" />
        <el-table-column prop="cycleDays" label="周期(天)" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.active"
              :loading="scope.row.switching"
              @change="handleStatusChange(scope.row)"
              active-text="启用"
              inactive-text="停用"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" link @click="openEditDialog(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">设备账户</div>
      </template>
      <el-table :data="accounts" style="width: 100%">
        <el-table-column prop="equipmentId" label="设备ID" width="120" />
        <el-table-column prop="planId" label="套餐ID" width="120" />
        <el-table-column prop="balance" label="余额" width="120">
          <template #default="scope">
            ¥{{ scope.row.balance?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="expireAt" label="到期时间">
          <template #default="scope">
            {{ scope.row.expireAt ? formatDate(scope.row.expireAt) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openRecharge(scope.row)">充值</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 套餐对话框 -->
    <el-dialog 
      v-model="showPlanDialog" 
      :title="isEdit ? '编辑套餐' : '新增套餐'" 
      width="520px"
      @closed="resetPlanForm"
    >
      <el-form :model="planForm" :rules="planRules" ref="planFormRef" label-width="90px">
        <el-form-item label="套餐名称" prop="planName">
          <el-input v-model="planForm.planName" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="计费模式" prop="billingMode">
          <el-select v-model="planForm.billingMode" placeholder="请选择计费模式" style="width: 100%">
            <el-option label="包月" value="MONTHLY" />
            <el-option label="按时长" value="TIME" />
            <el-option label="按流量" value="TRAFFIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number 
            v-model="planForm.price" 
            :min="0" 
            :precision="2" 
            :step="10"
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="planForm.unit" placeholder="如: 元/月" />
        </el-form-item>
        <el-form-item label="周期(天)" prop="cycleDays">
          <el-input-number 
            v-model="planForm.cycleDays" 
            :min="1" 
            :step="30"
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="状态" prop="active">
          <el-switch
            v-model="planForm.active"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPlanDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPlan">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 充值对话框 -->
    <el-dialog v-model="showRechargeDialog" title="账户充值" width="420px">
      <el-form :model="rechargeForm" label-width="80px">
        <el-form-item label="金额">
          <el-input-number 
            v-model="rechargeForm.amount" 
            :min="0" 
            :precision="2" 
            :step="100"
            style="width: 100%" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRechargeDialog = false">取消</el-button>
        <el-button type="primary" @click="recharge">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, Edit, Delete } from "@element-plus/icons-vue";
import http from "../api/http";

interface BillingPlan {
  id: number;
  planName: string;
  billingMode: string;
  price: number;
  unit: string;
  cycleDays: number;
  active: boolean;
  createdAt?: string;
  switching?: boolean;
}

const plans = ref<BillingPlan[]>([]);
const accounts = ref<any[]>([]);
const loading = ref(false);
const searchText = ref("");
const showPlanDialog = ref(false);
const showRechargeDialog = ref(false);
const submitting = ref(false);
const isEdit = ref(false);
const editingId = ref<number | null>(null);
const rechargeTargetId = ref<number | null>(null);

const planFormRef = ref();
const planForm = reactive({
  planName: "",
  billingMode: "MONTHLY",
  price: 0,
  unit: "元/月",
  cycleDays: 30,
  active: true
});

const rechargeForm = reactive({
  amount: 0
});

const planRules = {
  planName: [
    { required: true, message: "请输入套餐名称", trigger: "blur" }
  ],
  billingMode: [
    { required: true, message: "请选择计费模式", trigger: "change" }
  ],
  price: [
    { required: true, message: "请输入单价", trigger: "blur" }
  ],
  unit: [
    { required: true, message: "请输入单位", trigger: "blur" }
  ],
  cycleDays: [
    { required: true, message: "请输入周期", trigger: "blur" }
  ]
};

const filteredPlans = computed(() => {
  if (!searchText.value) return plans.value;
  return plans.value.filter(plan => 
    plan.planName.toLowerCase().includes(searchText.value.toLowerCase())
  );
});

const getBillingModeType = (mode: string) => {
  const typeMap: Record<string, string> = {
    'MONTHLY': 'success',
    'TIME': 'warning',
    'TRAFFIC': 'info'
  };
  return typeMap[mode] || 'info';
};

const getBillingModeText = (mode: string) => {
  const textMap: Record<string, string> = {
    'MONTHLY': '包月',
    'TIME': '按时长',
    'TRAFFIC': '按流量'
  };
  return textMap[mode] || mode;
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

const handleSearch = () => {
  // 搜索过滤在 computed 中处理
};

const fetchPlans = async () => {
  loading.value = true;
  try {
    const res: any = await http.get("/billing/plans");
    plans.value = res.data || [];
  } finally {
    loading.value = false;
  }
};

const fetchAccounts = async () => {
  const res: any = await http.get("/billing/accounts");
  accounts.value = res.data || [];
};

const openCreateDialog = () => {
  isEdit.value = false;
  editingId.value = null;
  resetPlanForm();
  showPlanDialog.value = true;
};

const openEditDialog = (row: BillingPlan) => {
  isEdit.value = true;
  editingId.value = row.id;
  planForm.planName = row.planName;
  planForm.billingMode = row.billingMode;
  planForm.price = row.price;
  planForm.unit = row.unit;
  planForm.cycleDays = row.cycleDays;
  planForm.active = row.active;
  showPlanDialog.value = true;
};

const resetPlanForm = () => {
  planForm.planName = "";
  planForm.billingMode = "MONTHLY";
  planForm.price = 0;
  planForm.unit = "元/月";
  planForm.cycleDays = 30;
  planForm.active = true;
};

const submitPlan = async () => {
  if (!planFormRef.value) return;
  
  await planFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true;
      try {
        if (isEdit.value && editingId.value) {
          await http.put(`/billing/plans/${editingId.value}`, planForm);
          ElMessage.success("套餐已更新");
        } else {
          await http.post("/billing/plans", planForm);
          ElMessage.success("套餐已创建");
        }
        showPlanDialog.value = false;
        fetchPlans();
      } finally {
        submitting.value = false;
      }
    }
  });
};

const handleStatusChange = async (row: BillingPlan) => {
  row.switching = true;
  try {
    await http.put(`/billing/plans/${row.id}`, { active: row.active });
    ElMessage.success(row.active ? "套餐已启用" : "套餐已停用");
  } catch {
    row.active = !row.active;
  } finally {
    row.switching = false;
  }
};

const handleDelete = async (row: BillingPlan) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除套餐 "${row.planName}" 吗？`,
      "删除确认",
      {
        confirmButtonText: "删除",
        cancelButtonText: "取消",
        type: "warning"
      }
    );
    
    await http.delete(`/billing/plans/${row.id}`);
    ElMessage.success("套餐已删除");
    fetchPlans();
  } catch {
    // 用户取消或请求失败
  }
};

const openRecharge = (row: any) => {
  rechargeTargetId.value = row.id;
  rechargeForm.amount = 0;
  showRechargeDialog.value = true;
};

const recharge = async () => {
  if (!rechargeTargetId.value) return;
  await http.post(`/billing/accounts/${rechargeTargetId.value}/recharge`, rechargeForm);
  showRechargeDialog.value = false;
  ElMessage.success("充值成功");
  fetchAccounts();
};

onMounted(() => {
  fetchPlans();
  fetchAccounts();
});
</script>

<style scoped>
.page {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
}

.page-sub {
  color: #8c8c9a;
  margin-top: 4px;
  font-size: 13px;
}

.page-header :deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
}

.page-header :deep(.el-button + .el-button) {
  margin-left: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.table-card {
  margin-top: 16px;
}

:deep(.el-card) {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

:deep(.el-table) {
  border-radius: 12px;
}

:deep(.el-table th) {
  background: #fafbfc !important;
  font-weight: 600;
}

:deep(.el-button + .el-button) {
  margin-left: 8px;
}
</style>
