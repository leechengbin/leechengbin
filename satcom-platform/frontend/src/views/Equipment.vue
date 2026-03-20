<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">设备管理</div>
        <div class="page-sub">设备全生命周期档案与远程控制</div>
      </div>
      <el-button type="primary" @click="showDialog = true">新增设备</el-button>
    </div>

    <el-card>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索资产编号/型号" style="width: 240px" />
        <el-button type="primary" @click="search">查询</el-button>
        <div class="toolbar-total">共 {{ total }} 条</div>
      </div>
      <el-table :data="displayRows" style="width: 100%">
        <el-table-column prop="assetCode" label="资产编号" width="140" />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="ownerUnit" label="所属单位" />
        <el-table-column prop="firmwareVersion" label="固件版本" width="140" />
        <el-table-column label="锁定" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.locked ? 'danger' : 'success'">
              {{ scope.row.locked ? '锁定' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button size="small" type="info" @click="edit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="confirmDelete(scope.row)">删除</el-button>
            <el-button size="small" @click="toggleLock(scope.row)">
              {{ scope.row.locked ? '解锁' : '锁机' }}
            </el-button>
            <el-button size="small" type="primary" @click="openRecord(scope.row)">添加记录</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑设备' : '新增设备'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="资产编号">
          <el-input v-model="form.assetCode" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.model" />
        </el-form-item>
        <el-form-item label="状态">
          <el-input v-model="form.status" />
        </el-form-item>
        <el-form-item label="所属单位">
          <el-input v-model="form.ownerUnit" />
        </el-form-item>
        <el-form-item label="固件版本">
          <el-input v-model="form.firmwareVersion" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input v-model.number="form.latitude" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input v-model.number="form.longitude" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="create">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRecordDialog" title="新增维护记录" width="520px">
      <el-form :model="recordForm" label-width="90px">
        <el-form-item label="类型">
          <el-input v-model="recordForm.recordType" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="recordForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="recordForm.recordAt" type="datetime" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRecordDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRecord">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDeleteDialog" title="确认删除" width="400px">
      <p>确定要删除该设备吗？此操作不可恢复。</p>
      <template #footer>
        <el-button @click="showDeleteDialog = false">取消</el-button>
        <el-button type="danger" @click="deleteEquipment">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import http from "../api/http";

const rows = ref([] as any[]);
const keyword = ref("");
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const remotePaged = ref(false);

const displayRows = computed(() => {
  if (remotePaged.value) {
    return rows.value;
  }
  const start = (page.value - 1) * pageSize.value;
  return rows.value.slice(start, start + pageSize.value);
});
const showDialog = ref(false);
const showRecordDialog = ref(false);
const showDeleteDialog = ref(false);
const isEdit = ref(false);
const currentEquipmentId = ref<number | null>(null);
const deleteId = ref<number | null>(null);

const form = reactive({
  assetCode: "",
  model: "",
  status: "",
  ownerUnit: "",
  firmwareVersion: "",
  latitude: null,
  longitude: null
});

const recordForm = reactive({
  recordType: "",
  description: "",
  recordAt: ""
});

const fetchData = async () => {
  try {
    const res: any = await http.get("/equipment", {
      params: {
        keyword: keyword.value,
        page: page.value,
        size: pageSize.value
      }
    });
    if (res.success) {
      const records = res.data?.records ?? res.data ?? [];
      const apiTotal = res.data?.total;
      const totalFromApi = apiTotal == null || (apiTotal === 0 && records.length > 0)
        ? records.length
        : apiTotal;
      const looksPaged = Array.isArray(res.data?.records)
        && records.length <= pageSize.value
        && totalFromApi >= records.length;

      rows.value = records;
      total.value = totalFromApi;
      remotePaged.value = looksPaged;

      if (!remotePaged.value && rows.value.length > 0) {
        const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value));
        if (page.value > maxPage) {
          page.value = maxPage;
        }
      }
    } else {
      console.error('获取设备列表失败:', res.message);
    }
  } catch (error) {
    console.error('获取设备列表错误:', error);
  }
};

const handleCurrentChange = () => {
  if (remotePaged.value) {
    fetchData();
  }
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  page.value = 1;
  if (remotePaged.value) {
    fetchData();
  }
};

const create = async () => {
  if (isEdit.value && currentEquipmentId.value) {
    await http.put(`/equipment/${currentEquipmentId.value}`, form);
  } else {
    await http.post("/equipment", form);
  }
  showDialog.value = false;
  Object.assign(form, {
    assetCode: "",
    model: "",
    status: "",
    ownerUnit: "",
    firmwareVersion: "",
    latitude: null,
    longitude: null
  });
  isEdit.value = false;
  currentEquipmentId.value = null;
  page.value = 1;
  fetchData();
};

const edit = (row: any) => {
  isEdit.value = true;
  currentEquipmentId.value = row.id;
  Object.assign(form, {
    assetCode: row.assetCode,
    model: row.model,
    status: row.status,
    ownerUnit: row.ownerUnit,
    firmwareVersion: row.firmwareVersion,
    latitude: row.latitude,
    longitude: row.longitude
  });
  showDialog.value = true;
};

const confirmDelete = (row: any) => {
  deleteId.value = row.id;
  showDeleteDialog.value = true;
};

const deleteEquipment = async () => {
  if (!deleteId.value) return;
  await http.delete(`/equipment/${deleteId.value}`);
  showDeleteDialog.value = false;
  deleteId.value = null;
  fetchData();
};

const search = () => {
  page.value = 1;
  fetchData();
};

const toggleLock = async (row: any) => {
  await http.post(`/equipment/${row.id}/lock`, { locked: !row.locked });
  fetchData();
};

const openRecord = (row: any) => {
  currentEquipmentId.value = row.id;
  showRecordDialog.value = true;
};

const saveRecord = async () => {
  if (!currentEquipmentId.value) return;
  await http.post(`/equipment/${currentEquipmentId.value}/records`, recordForm);
  showRecordDialog.value = false;
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

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
  padding: 16px 20px;
  background: #fafbfc;
  border-radius: 10px;
  border: 1px solid #f0f0f5;
}

.toolbar :deep(.el-input) {
  border-radius: 8px;
}

.toolbar :deep(.el-button) {
  border-radius: 8px;
}

.toolbar-total {
  margin-left: auto;
  color: #8c8c9a;
  font-size: 13px;
}

.toolbar :deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

.toolbar :deep(.el-table th) {
  background: #fafbfc !important;
  font-weight: 600;
  color: #26263a;
}

.toolbar :deep(.el-button + .el-button) {
  margin-left: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 10px;
}
</style>
