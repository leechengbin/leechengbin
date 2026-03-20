<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">角色管理</div>
        <div class="page-sub">角色配置与菜单权限管理</div>
      </div>
      <el-button type="primary" @click="showDialog = true">新增角色</el-button>
    </div>

    <el-card>
      <el-table :data="rows" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="角色编码" width="160" />
        <el-table-column prop="name" label="角色名称" width="160" />
        <el-table-column prop="description" label="角色描述" />
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button size="small" type="primary" @click="editRole(scope.row)">编辑</el-button>
            <el-button size="small" type="success" @click="openMenuPermissionDialog(scope.row)">菜单权限</el-button>
            <el-button size="small" type="danger" @click="deleteRole(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="dialogTitle" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色编码">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showMenuPermissionDialog" title="菜单权限配置" width="720px">
      <div v-if="loadingMenus" class="loading">加载中...</div>
      <div v-else>
        <el-checkbox v-model="selectAllMenus" @change="handleSelectAllMenus">全选</el-checkbox>
        <el-tree
          :data="menuTree"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedMenuIds"
          @check="handleMenuCheck"
          style="margin-top: 12px"
        >
          <template #default="{ node, data }">
            <span>{{ data.name }}</span>
          </template>
        </el-tree>
      </div>
      <template #footer>
        <el-button @click="showMenuPermissionDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMenuPermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from "vue";
import http from "../api/http";
import { ElMessage } from 'element-plus';

const rows = ref<any[]>([]);
const showDialog = ref(false);
const showMenuPermissionDialog = ref(false);
const dialogTitle = ref('新增角色');
const loadingMenus = ref(false);
const selectAllMenus = ref(false);
const currentRole = ref<any>(null);
const menuList = ref<any[]>([]);
const checkedMenuIds = ref<number[]>([]);

const form = reactive({
  id: null,
  code: "",
  name: "",
  description: ""
});

// 构建菜单树
const menuTree = computed(() => {
  const buildTree = (items: any[], parentId = 0) => {
    return items
      .filter(item => item.parentId === parentId)
      .map(item => ({
        ...item,
        children: buildTree(items, item.id)
      }));
  };
  return buildTree(menuList.value);
});

const fetchData = async () => {
  try {
    const res: any = await http.get("/roles");
    if (res.success) {
      rows.value = res.data;
    }
  } catch (error) {
    console.error('获取角色列表失败:', error);
    ElMessage.error('获取角色列表失败');
  }
};

const editRole = (role: any) => {
  Object.assign(form, role);
  dialogTitle.value = '编辑角色';
  showDialog.value = true;
};

const saveRole = async () => {
  try {
    if (form.id) {
      await http.put(`/roles/${form.id}`, form);
      ElMessage.success('角色更新成功');
    } else {
      await http.post("/roles", form);
      ElMessage.success('角色创建成功');
    }
    showDialog.value = false;
    resetForm();
    fetchData();
  } catch (error) {
    console.error('保存角色失败:', error);
    ElMessage.error('保存角色失败');
  }
};

const deleteRole = async (id: number) => {
  try {
    await http.delete(`/roles/${id}`);
    ElMessage.success('角色删除成功');
    fetchData();
  } catch (error) {
    console.error('删除角色失败:', error);
    ElMessage.error('删除角色失败');
  }
};

const openMenuPermissionDialog = async (role: any) => {
  currentRole.value = role;
  loadingMenus.value = true;
  try {
    // 获取所有菜单
    const menuRes: any = await http.get("/menus");
    if (menuRes.success) {
      menuList.value = menuRes.data;
    }
    
    // 获取角色已有的菜单权限
    const roleMenuRes: any = await http.get(`/roles/${role.id}/menus`);
    if (roleMenuRes.success) {
      checkedMenuIds.value = roleMenuRes.data;
    }
  } catch (error) {
    console.error('加载菜单权限失败:', error);
    ElMessage.error('加载菜单权限失败');
  } finally {
    loadingMenus.value = false;
  }
  showMenuPermissionDialog.value = true;
};

const handleSelectAllMenus = (value: boolean) => {
  if (value) {
    // 全选所有菜单ID
    const allMenuIds = menuList.value.map(menu => menu.id);
    checkedMenuIds.value = allMenuIds;
  } else {
    // 取消全选
    checkedMenuIds.value = [];
  }
};

const handleMenuCheck = () => {
  // 这里可以添加额外的逻辑
};

const saveMenuPermissions = async () => {
  if (!currentRole.value) return;
  
  try {
    await http.post(`/roles/${currentRole.value.id}/menus`, checkedMenuIds.value);
    ElMessage.success('菜单权限保存成功');
    showMenuPermissionDialog.value = false;
  } catch (error) {
    console.error('保存菜单权限失败:', error);
    ElMessage.error('保存菜单权限失败');
  }
};

const resetForm = () => {
  Object.assign(form, {
    id: null,
    code: "",
    name: "",
    description: ""
  });
  dialogTitle.value = '新增角色';
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

:deep(.el-table .cell) {
  white-space: nowrap;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #8c8c9a;
}
</style>