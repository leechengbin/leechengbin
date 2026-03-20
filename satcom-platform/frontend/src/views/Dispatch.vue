<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">应急调度</div>
        <div class="page-sub">指令下发与任务跟踪</div>
      </div>
      <el-button type="primary" @click="showDialog = true">新建任务</el-button>
    </div>

    <el-card>
      <el-table :data="rows" style="width: 100%" v-loading="loading" empty-text="暂无任务数据">
        <el-table-column prop="title" label="任务标题" />
        <el-table-column prop="level" label="等级" width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="targetUnit" label="目标单位" width="160" />
        <el-table-column prop="contact" label="联系人" width="140" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="primary" @click="edit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="confirmDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" title="任务信息" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.level" placeholder="选择">
            <el-option label="一般" value="NORMAL" />
            <el-option label="重要" value="IMPORTANT" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="选择">
            <el-option label="新建" value="NEW" />
            <el-option label="执行中" value="RUNNING" />
            <el-option label="已完成" value="DONE" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标单位">
          <el-input v-model="form.targetUnit" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contact" />
        </el-form-item>
        <el-form-item label="指令内容">
          <el-input v-model="form.message" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="save">提交</el-button>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="showDeleteDialog" title="确认删除" width="400px">
      <p>确定要删除任务 "{{ deleteTaskTitle }}" 吗？此操作无法撤销。</p>
      <template #footer>
        <el-button @click="showDeleteDialog = false">取消</el-button>
        <el-button type="danger" @click="deleteTask">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import { onMounted, reactive, ref } from "vue";
import http from "../api/http";

const rows = ref<any[]>([]);
const loading = ref(false);
const showDialog = ref(false);
const showDeleteDialog = ref(false);
const editingId = ref<number | null>(null);
const deletingId = ref<number | null>(null);
const deleteTaskTitle = ref("");

const form = reactive({
  title: "",
  level: "NORMAL",
  status: "NEW",
  targetUnit: "",
  contact: "",
  message: ""
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res: any = await http.get("/dispatch/tasks");
    if (res.success) {
      rows.value = res.data || [];
    } else {
      rows.value = [];
    }
  } catch (error) {
    console.error("获取任务列表失败:", error);
    rows.value = [];
  } finally {
    loading.value = false;
  }
};

const save = async () => {
  try {
    if (editingId.value) {
      await http.put(`/dispatch/tasks/${editingId.value}`, form);
      ElMessage.success("任务更新成功");
    } else {
      await http.post("/dispatch/tasks", form);
      ElMessage.success("任务创建成功");
    }
    showDialog.value = false;
    editingId.value = null;
    resetForm();
    fetchData();
  } catch (error) {
    console.error("保存失败:", error);
    ElMessage.error("保存失败");
  }
};

const resetForm = () => {
  form.title = "";
  form.level = "NORMAL";
  form.status = "NEW";
  form.targetUnit = "";
  form.contact = "";
  form.message = "";
};

const edit = (row: any) => {
  Object.assign(form, row);
  editingId.value = row.id;
  showDialog.value = true;
};

const confirmDelete = (row: any) => {
  deletingId.value = row.id;
  deleteTaskTitle.value = row.title;
  showDeleteDialog.value = true;
};

const deleteTask = async () => {
  try {
    await http.delete(`/dispatch/tasks/${deletingId.value}`);
    ElMessage.success("任务已删除");
    showDeleteDialog.value = false;
    deletingId.value = null;
    deleteTaskTitle.value = "";
    fetchData();
  } catch (error) {
    console.error("删除失败:", error);
    ElMessage.error("删除失败");
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
