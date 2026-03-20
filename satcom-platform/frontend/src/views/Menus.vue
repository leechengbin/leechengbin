<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">菜单管理</div>
        <div class="page-sub">系统菜单配置与管理</div>
      </div>
      <el-button type="primary" @click="showDialog = true">新增菜单</el-button>
    </div>

    <el-card>
      <el-table :data="rows" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="菜单名称" width="160" />
        <el-table-column prop="path" label="路由路径" width="180" />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="parentId" label="父菜单ID" width="100" />
        <el-table-column prop="menuOrder" label="排序" width="80" />
        <el-table-column prop="visible" label="可见" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.visible ? 'success' : 'warning'">
              {{ scope.row.visible ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="editMenu(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteMenu(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="dialogTitle" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="菜单名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" />
        </el-form-item>
        <el-form-item label="父菜单ID">
          <el-input v-model.number="form.parentId" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input v-model.number="form.menuOrder" />
        </el-form-item>
        <el-form-item label="可见">
          <el-switch v-model="form.visible" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMenu">保存</el-button>
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
const dialogTitle = ref('新增菜单');

const form = reactive({
  id: null,
  name: "",
  path: "",
  icon: "",
  parentId: 0,
  menuOrder: 0,
  visible: true
});

const fetchData = async () => {
  try {
    const res: any = await http.get("/menus");
    if (res.success) {
      rows.value = res.data;
    }
  } catch (error) {
    console.error('获取菜单列表失败:', error);
    ElMessage.error('获取菜单列表失败');
  }
};

const editMenu = (menu: any) => {
  Object.assign(form, menu);
  dialogTitle.value = '编辑菜单';
  showDialog.value = true;
};

const saveMenu = async () => {
  try {
    if (form.id) {
      await http.put(`/menus/${form.id}`, form);
      ElMessage.success('菜单更新成功');
    } else {
      await http.post("/menus", form);
      ElMessage.success('菜单创建成功');
    }
    showDialog.value = false;
    resetForm();
    fetchData();
  } catch (error) {
    console.error('保存菜单失败:', error);
    ElMessage.error('保存菜单失败');
  }
};

const deleteMenu = async (id: number) => {
  try {
    await http.delete(`/menus/${id}`);
    ElMessage.success('菜单删除成功');
    fetchData();
  } catch (error) {
    console.error('删除菜单失败:', error);
    ElMessage.error('删除菜单失败');
  }
};

const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: "",
    path: "",
    icon: "",
    parentId: 0,
    menuOrder: 0,
    visible: true
  });
  dialogTitle.value = '新增菜单';
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

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-button + .el-button) {
  margin-left: 8px;
}
</style>