<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">用户与权限</div>
        <div class="page-sub">账号管理与角色配置</div>
      </div>
      <el-button type="primary" @click="showDialog = true">新增账号</el-button>
    </div>

    <el-card>
      <el-table :data="rows" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="160" />
        <el-table-column prop="displayName" label="显示名" width="160" />
        <el-table-column prop="roleCode" label="角色" width="140" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'warning'">
              {{ scope.row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button size="small" type="info" @click="edit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="confirmDelete(scope.row)">删除</el-button>
            <el-button size="small" type="primary" @click="openPermissionDialog(scope.row)">权限配置</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑账号' : '新增账号'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="显示名">
          <el-input v-model="form.displayName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCode">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="运维" value="OP" />
            <el-option label="财务" value="FIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPermissionDialog" title="权限配置" width="720px">
      <div v-if="loadingPermissions" class="loading">加载中...</div>
      <div v-else>
        <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
        <el-table :data="equipmentList" style="width: 100%">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="assetCode" label="资产编号" width="120" />
          <el-table-column prop="model" label="型号" width="100" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="ownerUnit" label="所属单位" />
          <el-table-column label="查看权限" width="100">
            <template #default="scope">
              <el-checkbox v-model="scope.row.viewPermission" />
            </template>
          </el-table-column>
          <el-table-column label="编辑权限" width="100">
            <template #default="scope">
              <el-checkbox v-model="scope.row.editPermission" />
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showPermissionDialog = false">取消</el-button>
        <el-button type="primary" @click="savePermissions">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDeleteDialog" title="确认删除" width="400px">
      <p>确定要删除该用户吗？此操作不可恢复。</p>
      <template #footer>
        <el-button @click="showDeleteDialog = false">取消</el-button>
        <el-button type="danger" @click="deleteUser">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import http from "../api/http";
import { ElMessage } from 'element-plus';

const rows = ref<any[]>([]);
const showDialog = ref(false);
const showPermissionDialog = ref(false);
const showDeleteDialog = ref(false);
const loadingPermissions = ref(false);
const selectAll = ref(false);
const isEdit = ref(false);
const currentUser = ref<any>(null);
const deleteId = ref<number | null>(null);
const equipmentList = ref<any[]>([]);

const form = reactive({
  username: "",
  displayName: "",
  roleCode: "ADMIN",
  enabled: true
});

const fetchData = async () => {
  const res: any = await http.get("/users");
  rows.value = res.data;
};

const save = async () => {
  if (isEdit.value && currentUser.value) {
    await http.put(`/users/${currentUser.value.id}`, form);
  } else {
    await http.post("/users", form);
  }
  showDialog.value = false;
  isEdit.value = false;
  currentUser.value = null;
  fetchData();
};

const edit = (row: any) => {
  isEdit.value = true;
  currentUser.value = row;
  Object.assign(form, {
    username: row.username,
    displayName: row.displayName,
    roleCode: row.roleCode,
    enabled: row.enabled
  });
  showDialog.value = true;
};

const confirmDelete = (row: any) => {
  deleteId.value = row.id;
  showDeleteDialog.value = true;
};

const deleteUser = async () => {
  if (!deleteId.value) return;
  await http.delete(`/users/${deleteId.value}`);
  showDeleteDialog.value = false;
  deleteId.value = null;
  fetchData();
};

const fetchAllEquipment = async () => {
  const all: any[] = [];
  let page = 1;
  const size = 200;
  let total = 0;

  do {
    const res: any = await http.get("/equipment", {
      params: { page, size, all: true }
    });
    if (!res.success) {
      throw new Error(res.message || "获取设备列表失败");
    }
    const records = res.data.records || [];
    total = res.data.total || 0;
    all.push(...records);
    page += 1;
  } while (all.length < total);

  return all;
};

const openPermissionDialog = async (user: any) => {
  currentUser.value = user;
  loadingPermissions.value = true;
  try {
    // 获取所有设备（分页拉取）
    const equipments = await fetchAllEquipment();

    // 获取用户已有的权限
    const userPermissionsRes: any = await http.get(`/users/${user.id}/equipment-permissions`);
    const userPermissions = userPermissionsRes.data || [];

    // 构建设备权限列表
    equipmentList.value = equipments.map((eq: any) => {
      const permission = userPermissions.find((p: any) => p.equipmentId === eq.id);
      return {
        ...eq,
        viewPermission: permission ? permission.viewPermission : false,
        editPermission: permission ? permission.editPermission : false
      };
    });
  } catch (error) {
    console.error('加载权限失败:', error);
    ElMessage.error('加载权限失败');
  } finally {
    loadingPermissions.value = false;
  }
  showPermissionDialog.value = true;
};

const handleSelectAll = (value: boolean) => {
  equipmentList.value.forEach(item => {
    item.viewPermission = value;
    item.editPermission = value;
  });
};

const savePermissions = async () => {
  if (!currentUser.value) return;
  
  try {
    const permissions = equipmentList.value.map(item => ({
      equipmentId: item.id,
      viewPermission: item.viewPermission,
      editPermission: item.editPermission
    }));
    
    await http.post(`/users/${currentUser.value.id}/equipment-permissions`, permissions);
    ElMessage.success('权限保存成功');
    showPermissionDialog.value = false;
  } catch (error) {
    console.error('保存权限失败:', error);
    ElMessage.error('保存权限失败');
  }
};

onMounted(fetchData);
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
